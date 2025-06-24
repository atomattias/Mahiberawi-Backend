# Mahiberawi Backend API Testing Script
Write-Host "Testing Mahiberawi Backend API" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"

# Test 1: Health Check
Write-Host "`n1. Testing Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET
    Write-Host "SUCCESS - Health Check: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Health Check: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: User Registration
Write-Host "`n2. Testing User Registration..." -ForegroundColor Yellow
$registerData = @{
    firstName = "Test"
    lastName = "User"
    email = "test@example.com"
    password = "TestPassword123!"
    phoneNumber = "+1234567890"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -ContentType "application/json"
    Write-Host "SUCCESS - Registration: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Registration: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

# Test 3: User Login
Write-Host "`n3. Testing User Login..." -ForegroundColor Yellow
$loginData = @{
    email = "test@example.com"
    password = "TestPassword123!"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "SUCCESS - Login: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    
    # Extract token for further tests
    $loginResponse = $response.Content | ConvertFrom-Json
    if ($loginResponse.accessToken) {
        $global:authToken = $loginResponse.accessToken
        Write-Host "Token extracted for further tests" -ForegroundColor Green
    }
} catch {
    Write-Host "FAILED - Login: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Admin Promotion (if we have a token)
if ($global:authToken) {
    Write-Host "`n4. Testing Admin Promotion..." -ForegroundColor Yellow
    $headers = @{
        "Authorization" = "Bearer $global:authToken"
        "Content-Type" = "application/json"
    }
    
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote/test@example.com" -Method POST -Headers $headers
        Write-Host "SUCCESS - Admin Promotion: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    } catch {
        Write-Host "FAILED - Admin Promotion: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nTesting Complete!" -ForegroundColor Green 