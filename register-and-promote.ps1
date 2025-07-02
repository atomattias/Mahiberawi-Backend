# Register and Promote mattiasgebrie@gmail.com to Super Admin Script
Write-Host "Registering and Promoting mattiasgebrie@gmail.com to Super Admin" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$email = "mattiasgebrie@gmail.com"
$password = "TestPassword123!"

Write-Host "`nTarget user: $email" -ForegroundColor Yellow
Write-Host "Backend URL: $baseUrl" -ForegroundColor Cyan

# Step 1: Try to register the user first
Write-Host "`n1. Attempting to register user..." -ForegroundColor Yellow
$registerData = @{
    firstName = "Mattias"
    lastName = "Gabrie"
    email = $email
    password = $password
    confirmPassword = $password
    phoneNumber = "+1234567890"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -ContentType "application/json"
    Write-Host "SUCCESS - Registration: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "Registration attempt result: $($_.Exception.Message)" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Registration Details: $errorContent" -ForegroundColor Yellow
    }
}

# Step 2: Login to get authentication token
Write-Host "`n2. Attempting to login..." -ForegroundColor Yellow
$loginData = @{
    email = $email
    password = $password
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "SUCCESS - Login: $($response.StatusCode)" -ForegroundColor Green
    
    # Extract token
    $loginResponse = $response.Content | ConvertFrom-Json
    if ($loginResponse.accessToken) {
        $authToken = $loginResponse.accessToken
        Write-Host "Token extracted successfully" -ForegroundColor Green
    } else {
        Write-Host "No access token found in response" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "FAILED - Login: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Login Error Details: $errorContent" -ForegroundColor Red
    }
    exit 1
}

# Step 3: Try to promote to Super Admin (without auth first)
Write-Host "`n3. Attempting to promote without authentication..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote/$email" -Method POST
    Write-Host "SUCCESS - Promotion (no auth): $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "Promotion without auth failed: $($_.Exception.Message)" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Yellow
    }
}

# Step 4: Try to promote to Super Admin with authentication
Write-Host "`n4. Attempting to promote with authentication..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $authToken"
    "Content-Type" = "application/json"
}

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote/$email" -Method POST -Headers $headers
    Write-Host "SUCCESS - Promotion (with auth): $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Promotion (with auth): $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

# Step 5: Verify the promotion
Write-Host "`n5. Verifying promotion..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/check-role/$email" -Method GET
    Write-Host "SUCCESS - Role Check: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Role Check: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nRegistration and promotion process complete!" -ForegroundColor Green 