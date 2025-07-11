# Normal Admin Promotion Script
Write-Host "Promoting User to Admin Role" -ForegroundColor Green
Write-Host "============================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"

Write-Host "`nThis script promotes a user to ADMIN role using Super Admin authentication" -ForegroundColor Yellow
Write-Host "You must be logged in as a Super Admin to use this script" -ForegroundColor Yellow

# Step 1: Get target email
$targetEmail = Read-Host "`nEnter the email of the user to promote to Admin"

# Step 2: Authenticate as Super Admin
Write-Host "`n1. Authenticating as Super Admin..." -ForegroundColor Yellow

$superAdminEmail = Read-Host "Enter your Super Admin email" 
$password = Read-Host "Enter your Super Admin password" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

try {
    $loginBody = @{
        email = $superAdminEmail
        password = $passwordPlain
    } | ConvertTo-Json

    $loginHeaders = @{
        "Content-Type" = "application/json"
    }

    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -Headers $loginHeaders
    Write-Host "SUCCESS - Authentication: $($loginResponse.StatusCode)" -ForegroundColor Green
    
    $loginData = $loginResponse.Content | ConvertFrom-Json
    $jwtToken = $loginData.data.accessToken
    
    Write-Host "JWT Token obtained successfully" -ForegroundColor Green
    
    # Set headers for authenticated requests
    $authHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $jwtToken"
    }
    
} catch {
    Write-Host "FAILED - Authentication: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
    exit 1
}

# Step 3: Promote user to Admin
Write-Host "`n2. Promoting $targetEmail to Admin role..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/admin/promote-to-admin/$targetEmail" -Method POST -Headers $authHeaders
    Write-Host "SUCCESS - Admin Promotion: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Admin Promotion: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nAdmin promotion process complete!" -ForegroundColor Green 