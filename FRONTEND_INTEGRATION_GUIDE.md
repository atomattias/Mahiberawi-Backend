# Frontend Integration Guide - Enhanced Group Permissions & Role Management

## Overview

This guide outlines the comprehensive improvements made to the backend to address all frontend developer concerns regarding group permissions, user roles, and API consistency.

## 🎯 Key Improvements

### 1. **Group-Specific User Role in API Responses**
- ✅ **Fixed**: All group-related endpoints now include `userRole` field
- ✅ **Enhanced**: New permission-based endpoints with detailed permission flags
- ✅ **Consistent**: Standardized response structure across all group endpoints

### 2. **Backend Permission Enforcement**
- ✅ **Implemented**: All permission checks moved to backend
- ✅ **Clear Errors**: 403 Forbidden with descriptive messages
- ✅ **Role-Based**: Permissions based on user's role in specific group

### 3. **Enhanced Invitation System**
- ✅ **Permission Checks**: Clear permission validation for invitations
- ✅ **Consistent Responses**: Standardized invitation response format
- ✅ **Error Handling**: Descriptive error messages for permission failures

### 4. **Event Creation Clarification**
- ✅ **Documented**: Only admins and moderators can create events
- ✅ **Enforced**: Backend permission checks implemented
- ✅ **Configurable**: Respects group settings for event creation

## 📋 New Endpoints

### 1. **Group Permissions Endpoint**
```http
GET /groups/{groupId}/permissions
```

**Response:**
```json
{
  "groupId": "group123",
  "groupName": "Study Group",
  "userRole": "admin",
  "canCreateEvents": true,
  "canCreatePosts": true,
  "canCreatePayments": true,
  "canSendInvitations": true,
  "canRevokeInvitations": true,
  "canManageMembers": true,
  "canUpdateGroupSettings": true,
  "canDeleteGroup": true,
  "canViewMembers": true,
  "canViewEvents": true,
  "canViewPosts": true,
  "canViewPayments": true,
  "canViewInvitations": true,
  "allowEventCreation": true,
  "allowMemberInvites": true,
  "allowMessagePosting": true,
  "paymentRequired": false,
  "requireApproval": false
}
```

### 2. **Enhanced Group Events Endpoint**
```http
GET /groups/{groupId}/events/with-permissions
```

**Response:**
```json
{
  "groupId": "group123",
  "groupName": "Study Group",
  "userRole": "admin",
  "events": [...],
  "canCreateEvents": true,
  "totalEvents": 5
}
```

### 3. **Enhanced Group Posts Endpoint**
```http
GET /groups/{groupId}/posts/with-permissions
```

**Response:**
```json
{
  "groupId": "group123",
  "groupName": "Study Group",
  "userRole": "member",
  "posts": [...],
  "canCreatePosts": true,
  "totalPosts": 12
}
```

### 4. **Enhanced Group Payments Endpoint**
```http
GET /groups/{groupId}/payments/with-permissions
```

**Response:**
```json
{
  "groupId": "group123",
  "groupName": "Study Group",
  "userRole": "moderator",
  "payments": [...],
  "canCreatePayments": true,
  "totalPayments": 3
}
```

### 5. **Permission-Based Creation Endpoints**

#### Create Event with Permission Check
```http
POST /groups/{groupId}/events/with-permission
```

#### Create Post with Permission Check
```http
POST /groups/{groupId}/posts/with-permission
```

#### Create Payment with Permission Check
```http
POST /groups/{groupId}/payments/with-permission
```

#### Create Invitation with Permission Check
```http
POST /groups/invitations/with-permission
```

#### Revoke Invitation with Permission Check
```http
DELETE /groups/invitations/{invitationId}/with-permission
```

#### Get Invitations with Permission Check
```http
GET /groups/{groupId}/invitations/with-permissions
```

## 🔐 Permission System

### User Roles
- **ADMIN**: Full control over group
- **MODERATOR**: Can create events, payments, send invitations
- **MEMBER**: Can create posts (if allowed), view content
- **Non-member**: No access

### Permission Matrix

| Action | Admin | Moderator | Member | Non-member |
|--------|-------|-----------|--------|------------|
| Create Events | ✅ | ✅ | ❌ | ❌ |
| Create Posts | ✅ | ✅ | ✅* | ❌ |
| Create Payments | ✅ | ✅ | ❌ | ❌ |
| Send Invitations | ✅ | ✅ | ❌ | ❌ |
| Manage Members | ✅ | ❌ | ❌ | ❌ |
| Update Settings | ✅ | ❌ | ❌ | ❌ |
| Delete Group | ✅ | ❌ | ❌ | ❌ |
| View Content | ✅ | ✅ | ✅ | ❌ |

*Only if group settings allow message posting

## 🚨 Error Handling

### Permission Denied (403)
```json
{
  "error": "You do not have permission to create events in this group. Your role: member",
  "status": 403,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Not a Member (403)
```json
{
  "error": "You are not a member of this group",
  "status": 403,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Group Not Found (404)
```json
{
  "error": "Group not found with id: group123",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🔄 Migration Guide

### 1. **Update API Calls**
Replace existing group-scoped endpoints with enhanced versions:

```javascript
// Before
const events = await fetch(`/api/groups/${groupId}/events`);

// After
const events = await fetch(`/api/groups/${groupId}/events/with-permissions`);
```

### 2. **Use Permission Endpoint**
Add permission checking to your app:

```javascript
// Get user permissions for a group
const permissions = await fetch(`/api/groups/${groupId}/permissions`);
const userPermissions = await permissions.json();

// Use permissions to show/hide UI elements
if (userPermissions.canCreateEvents) {
  showCreateEventButton();
}

if (userPermissions.canSendInvitations) {
  showInviteButton();
}
```

### 3. **Handle Enhanced Responses**
Update your response handling:

```javascript
// Enhanced events response
const response = await fetch(`/api/groups/${groupId}/events/with-permissions`);
const data = await response.json();

console.log(data.userRole); // "admin", "member", "moderator"
console.log(data.canCreateEvents); // true/false
console.log(data.events); // array of events
```

### 4. **Use Permission-Based Endpoints**
For creation actions, use the new permission-checked endpoints:

```javascript
// Create event with permission check
const eventResponse = await fetch(`/api/groups/${groupId}/events/with-permission`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(eventData)
});

if (eventResponse.status === 403) {
  const error = await eventResponse.json();
  showError(error.error); // "You do not have permission to create events..."
}
```

## 📱 Frontend Implementation Examples

### 1. **Dynamic UI Based on Permissions**
```javascript
async function loadGroupPermissions(groupId) {
  const response = await fetch(`/api/groups/${groupId}/permissions`);
  const permissions = await response.json();
  
  // Show/hide UI elements based on permissions
  document.getElementById('create-event-btn').style.display = 
    permissions.canCreateEvents ? 'block' : 'none';
    
  document.getElementById('invite-btn').style.display = 
    permissions.canSendInvitations ? 'block' : 'none';
    
  document.getElementById('manage-members-btn').style.display = 
    permissions.canManageMembers ? 'block' : 'none';
}
```

### 2. **Permission-Aware Event Creation**
```javascript
async function createEvent(groupId, eventData) {
  try {
    const response = await fetch(`/api/groups/${groupId}/events/with-permission`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(eventData)
    });
    
    if (response.ok) {
      const event = await response.json();
      showSuccess('Event created successfully!');
      refreshEventsList();
    } else if (response.status === 403) {
      const error = await response.json();
      showError(error.error);
    }
  } catch (error) {
    showError('Failed to create event');
  }
}
```

### 3. **Enhanced Group Details Display**
```javascript
async function loadGroupDetails(groupId) {
  const [groupResponse, permissionsResponse] = await Promise.all([
    fetch(`/api/groups/${groupId}`),
    fetch(`/api/groups/${groupId}/permissions`)
  ]);
  
  const group = await groupResponse.json();
  const permissions = await permissionsResponse.json();
  
  // Display group info with user role
  document.getElementById('group-name').textContent = group.name;
  document.getElementById('user-role').textContent = `Your role: ${group.userRole}`;
  
  // Show action buttons based on permissions
  if (permissions.canCreateEvents) {
    showCreateEventButton();
  }
  
  if (permissions.canSendInvitations) {
    showInviteButton();
  }
}
```

## 🔧 Testing Checklist

### 1. **Permission Testing**
- [ ] Test with admin user (should have all permissions)
- [ ] Test with moderator user (should have limited permissions)
- [ ] Test with member user (should have basic permissions)
- [ ] Test with non-member user (should have no permissions)

### 2. **Error Handling Testing**
- [ ] Test 403 errors for unauthorized actions
- [ ] Test 404 errors for non-existent groups
- [ ] Test 401 errors for unauthenticated requests

### 3. **UI Testing**
- [ ] Verify buttons show/hide based on permissions
- [ ] Verify error messages display correctly
- [ ] Verify user role displays correctly

### 4. **API Testing**
- [ ] Test all new endpoints
- [ ] Verify response structures
- [ ] Test permission-based endpoints

## 📝 Breaking Changes

### ✅ **No Breaking Changes**
- All existing endpoints continue to work
- Response structures are backward compatible
- New endpoints are additive

### 🔄 **Recommended Changes**
- Use new permission-based endpoints for better error handling
- Implement permission checking for dynamic UI
- Use enhanced response formats for better UX

## 🎉 Benefits

1. **Security**: All permissions enforced on backend
2. **Consistency**: Standardized response formats
3. **Clarity**: Clear error messages and permission information
4. **Flexibility**: Dynamic UI based on user permissions
5. **Maintainability**: Centralized permission logic

## 📞 Support

If you encounter any issues or need clarification:
1. Check the API documentation in Swagger UI
2. Review error messages for specific guidance
3. Test with different user roles to verify permissions
4. Use the permission endpoint to debug permission issues

---

**Happy coding! 🚀** 