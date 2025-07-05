# Test script for join-with-code endpoint using existing user
Write-Host "Testing Join with Code Endpoint (Existing User)" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api"
$email = "mattiasgebrie@gmail.com"
$password = "TestPassword123!"

# Step 1: Test health endpoint
Write-Host "`n1. Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET
    Write-Host "SUCCESS - Health Check: $($response.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAILED - Health Check: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 2: Login with existing user
Write-Host "`n2. Logging in with existing user..." -ForegroundColor Yellow
$loginData = @{
    email = $email
    password = $password
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "SUCCESS - Login: $($response.StatusCode)" -ForegroundColor Green
    
    $loginResponse = $response.Content | ConvertFrom-Json
    if ($loginResponse.accessToken) {
        $global:authToken = $loginResponse.accessToken
        Write-Host "Token extracted successfully" -ForegroundColor Green
        Write-Host "User ID: $($loginResponse.user.id)" -ForegroundColor Cyan
        Write-Host "User Role: $($loginResponse.user.role)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "FAILED - Login: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
    exit
}

# Step 3: Test join-with-code endpoint with authentication
Write-Host "`n3. Testing join-with-code with authentication..." -ForegroundColor Yellow
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

# Step 4: Test alternative endpoints
Write-Host "`n4. Testing alternative join endpoints..." -ForegroundColor Yellow

# Test /groups/join-by-code
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/join-by-code" -Method POST -Headers $headers -Body '{"groupCode":"4B0AHQGY"}' -ContentType "application/json"
    Write-Host "SUCCESS - join-by-code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - join-by-code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

# Test /groups/join
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/join" -Method POST -Headers $headers -Body '{"code":"4B0AHQGY"}' -ContentType "application/json"
    Write-Host "SUCCESS - join: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - join: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nTesting Complete!" -ForegroundColor Green 