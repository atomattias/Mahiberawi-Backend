# Test script for Threading and Reaction Features
Write-Host "Testing Threading and Reaction Features" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

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
    confirmPassword = "TestPassword123!"
    phoneNumber = "+1234567890"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -Body $registerData -ContentType "application/json"
    Write-Host "SUCCESS - User Registration: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
    $registerResponse = $response.Content | ConvertFrom-Json
    $token = $registerResponse.authResponse.token
    if (-not $token) {
        Write-Host "FAILED - No token returned in registration response. Cannot continue." -ForegroundColor Red
        exit
    }
    Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - User Registration: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
    exit
}

# Step 3: Create a test group
Write-Host "`n3. Creating Test Group..." -ForegroundColor Yellow
$groupData = @{
    name = "Test Group for Threading"
    description = "A test group to test threading and reactions"
    type = "PUBLIC"
    privacy = "PUBLIC"
    settings = @{
        allowEventCreation = $true
        allowMemberInvites = $true
        allowMessagePosting = $true
        paymentRequired = $false
        requireApproval = $false
        monthlyDues = 0
    }
} | ConvertTo-Json

try {
    $headers = @{ Authorization = "Bearer $token" }
    $response = Invoke-WebRequest -Uri "$baseUrl/groups" -Method POST -Body $groupData -ContentType "application/json" -Headers $headers
    $groupResponse = $response.Content | ConvertFrom-Json
    $groupId = $groupResponse.data.id
    Write-Host "SUCCESS - Group Creation: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Group ID: $groupId" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Group Creation: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 4: Create a main post
Write-Host "`n4. Creating Main Post..." -ForegroundColor Yellow
$postData = @{
    content = "This is a main post for testing threading and reactions"
    type = "GROUP"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/$groupId/posts" -Method POST -Body $postData -ContentType "application/json" -Headers $headers
    $postResponse = $response.Content | ConvertFrom-Json
    $mainPostId = $postResponse.data.id
    Write-Host "SUCCESS - Main Post Creation: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Main Post ID: $mainPostId" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Main Post Creation: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 5: Create a reply to the main post
Write-Host "`n5. Creating Reply Post..." -ForegroundColor Yellow
$replyData = @{
    content = "This is a reply to the main post"
    type = "GROUP"
    parentMessageId = $mainPostId
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/$groupId/posts" -Method POST -Body $replyData -ContentType "application/json" -Headers $headers
    $replyResponse = $response.Content | ConvertFrom-Json
    $replyPostId = $replyResponse.data.id
    Write-Host "SUCCESS - Reply Post Creation: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Reply Post ID: $replyPostId" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Reply Post Creation: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Step 6: Add a reaction to the main post
Write-Host "`n6. Adding Reaction to Main Post..." -ForegroundColor Yellow
$reactionData = @{
    reactionType = "like"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/messages/$mainPostId/reactions" -Method POST -Body $reactionData -ContentType "application/json" -Headers $headers
    Write-Host "SUCCESS - Reaction Added: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Reaction Addition: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 7: Add another reaction to the main post
Write-Host "`n7. Adding Another Reaction to Main Post..." -ForegroundColor Yellow
$reactionData2 = @{
    reactionType = "love"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/messages/$mainPostId/reactions" -Method POST -Body $reactionData2 -ContentType "application/json" -Headers $headers
    Write-Host "SUCCESS - Second Reaction Added: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Second Reaction Addition: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 8: Get reactions for the main post
Write-Host "`n8. Getting Reactions for Main Post..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/messages/$mainPostId/reactions" -Method GET -Headers $headers
    Write-Host "SUCCESS - Reactions Retrieved: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Reactions Retrieval: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 9: Get group posts to see threading and reactions
Write-Host "`n9. Getting Group Posts with Threading and Reactions..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/groups/$groupId/posts" -Method GET -Headers $headers
    Write-Host "SUCCESS - Group Posts Retrieved: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Group Posts Retrieval: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 10: Remove a reaction
Write-Host "`n10. Removing Reaction from Main Post..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/messages/$mainPostId/reactions/like" -Method DELETE -Headers $headers
    Write-Host "SUCCESS - Reaction Removed: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED - Reaction Removal: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n✅ Threading and Reaction Features Test Complete!" -ForegroundColor Green
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- Main Post ID: $mainPostId" -ForegroundColor Cyan
Write-Host "- Reply Post ID: $replyPostId" -ForegroundColor Cyan
Write-Host "- Group ID: $groupId" -ForegroundColor Cyan 