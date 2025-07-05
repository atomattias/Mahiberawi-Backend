# Delete test user script
Write-Host "Deleting test user test@example.com" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api"
$email = "test@example.com"

Write-Host "`nTarget user: $email" -ForegroundColor Yellow
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

# Step 1: Test health endpoint
Write-Host "`n1. Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET
    Write-Host "SUCCESS - Health Check: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAILED - Health Check: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 2: Delete the user
Write-Host "`n2. Deleting user..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/delete-user?email=$email" -Method DELETE
    Write-Host "SUCCESS - User Deletion: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - User Deletion: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
    }
}

Write-Host "`n✅ Test user deletion complete!" -ForegroundColor Green 