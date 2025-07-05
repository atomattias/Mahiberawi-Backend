# Test script for join-with-code endpoint
Write-Host "Testing Join with Code Endpoint" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api"

# Step 1: Test health endpoint
Write-Host "`n1. Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET
    Write-Host "SUCCESS - Health Check: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAILED - Health Check: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 2: Register a test user
Write-Host "`n2. Registering Test User..." -ForegroundColor Yellow
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
} catch {
    Write-Host "Registration failed (user might already exist): $($_.Exception.Message)" -ForegroundColor Yellow
}

# Step 3: Login to get token
Write-Host "`n3. Logging in..." -ForegroundColor Yellow
$loginData = @{
    email = "test@example.com"
    password = "TestPassword123!"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "SUCCESS - Login: $($response.StatusCode)" -ForegroundColor Green
    
    $loginResponse = $response.Content | ConvertFrom-Json
    if ($loginResponse.accessToken) {
        $global:authToken = $loginResponse.accessToken
        Write-Host "Token extracted successfully" -ForegroundColor Green
    }
} catch {
    Write-Host "FAILED - Login: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 4: Test join-with-code endpoint without authentication
Write-Host "`n4. Testing join-with-code without authentication..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/join-with-code?code=4B0AHQGY" -Method POST
    Write-Host "SUCCESS - Join without auth: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Expected failure - Join without auth: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
}

# Step 5: Test join-with-code endpoint with authentication
Write-Host "`n5. Testing join-with-code with authentication..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $global:authToken"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/join-with-code?code=4B0AHQGY" -Method POST -Headers $headers
    Write-Host "SUCCESS - Join with auth: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Join with auth: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

# Step 6: Test alternative endpoints
Write-Host "`n6. Testing alternative join endpoints..." -ForegroundColor Yellow

# Test /groups/join-by-code
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/join-by-code" -Method POST -Headers $headers -Body '{"groupCode":"4B0AHQGY"}' -ContentType "application/json"
    Write-Host "SUCCESS - join-by-code: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAILED - join-by-code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# Test /groups/join
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/join" -Method POST -Headers $headers -Body '{"code":"4B0AHQGY"}' -ContentType "application/json"
    Write-Host "SUCCESS - join: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAILED - join: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host "`nTesting Complete!" -ForegroundColor Green 