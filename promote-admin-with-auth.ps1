# Promote User to Super Admin with Authentication
Write-Host "Promoting mattiasgebrie@gmail.com to Super Admin" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

$baseUrl = "https://mahiberawi-backend-production.up.railway.app/api"
$targetEmail = "mattiasgebrie@gmail.com"

# First, we need to authenticate as a user to get a JWT token
# Let's try to login with the target user first
Write-Host "`nStep 1: Attempting to login as $targetEmail..." -ForegroundColor Yellow

# You'll need to provide the password for mattiasgebrie@gmail.com
$password = Read-Host "Enter password for $targetEmail" -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

$loginData = @{
    email = $targetEmail
    password = $plainPassword
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "SUCCESS - Login: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    
    $loginResponse = $response.Content | ConvertFrom-Json
    if ($loginResponse.token) {
        $authToken = $loginResponse.token
        Write-Host "Token obtained successfully" -ForegroundColor Green
        
        # Step 2: Check current role
        Write-Host "`nStep 2: Checking current role..." -ForegroundColor Yellow
        $headers = @{
            "Authorization" = "Bearer $authToken"
            "Content-Type" = "application/json"
        }
        
        try {
            $roleResponse = Invoke-WebRequest -Uri "$baseUrl/admin/check-role/$targetEmail" -Method GET -Headers $headers
            Write-Host "SUCCESS - Role Check: $($roleResponse.StatusCode)" -ForegroundColor Green
            Write-Host "Response: $($roleResponse.Content)" -ForegroundColor Cyan
        } catch {
            Write-Host "FAILED - Role Check: $($_.Exception.Message)" -ForegroundColor Red
        }
        
        # Step 3: Promote to Super Admin
        Write-Host "`nStep 3: Promoting to Super Admin..." -ForegroundColor Yellow
        try {
            $promoteResponse = Invoke-WebRequest -Uri "$baseUrl/admin/promote/$targetEmail" -Method POST -Headers $headers
            Write-Host "SUCCESS - Promotion: $($promoteResponse.StatusCode)" -ForegroundColor Green
            Write-Host "Response: $($promoteResponse.Content)" -ForegroundColor Cyan
        } catch {
            Write-Host "FAILED - Promotion: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.Exception.Response) {
                $errorResponse = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorResponse)
                $errorContent = $reader.ReadToEnd()
                Write-Host "Error Details: $errorContent" -ForegroundColor Red
            }
        }
        
    } else {
        Write-Host "No token in response" -ForegroundColor Red
        Write-Host "Available fields: $($loginResponse | Get-Member -MemberType NoteProperty | Select-Object -ExpandProperty Name)" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "FAILED - Login: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorContent = $reader.ReadToEnd()
        Write-Host "Error Details: $errorContent" -ForegroundColor Red
    }
}

Write-Host "`nPromotion process complete!" -ForegroundColor Green 