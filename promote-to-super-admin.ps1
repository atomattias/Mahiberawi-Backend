# Promote User to Super Admin with Auth Script

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$email = "mattiasgebrie@gmail.com"
$password = Read-Host -Prompt "Enter password for $email"

Write-Host "\nAuthenticating as $email..." -ForegroundColor Yellow

# 1. Authenticate and get JWT token
$loginBody = @{ email = $email; password = $password } | ConvertTo-Json
$loginResponse = $null
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "Login successful!" -ForegroundColor Green
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
    exit 1
}

# 2. Call promote endpoint with Authorization header
Write-Host "\nPromoting $email to SUPER_ADMIN..." -ForegroundColor Yellow
$headers = @{ Authorization = "Bearer $token" }
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote/$email" -Method POST -Headers $headers
    Write-Host "SUCCESS - User Promotion: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - User Promotion: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "\nPromotion attempt complete!" -ForegroundColor Green 