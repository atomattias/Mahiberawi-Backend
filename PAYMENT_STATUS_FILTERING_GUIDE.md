# Payment Status Filtering Guide

## Overview
This guide covers the new payment status filtering functionality that allows members to view their own payments and admins to view group payment details with status filtering.

## Payment Status Values
- **PENDING** = Unpaid (payment request sent, awaiting payment)
- **PAID** = Paid (payment successful)
- **FAILED** = Payment failed
- **CANCELLED** = Payment cancelled
- **REFUNDED** = Payment refunded

## New Endpoints

### 1. User Payment Filtering (For Members)

#### Get User's Payments with Status Filtering
```http
GET /api/payments/user/filtered?status=PENDING&status=PAID
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**
- `status` (optional): List of payment statuses to filter by
  - `PENDING` - Unpaid payments
  - `PAID` - Paid payments
  - `FAILED` - Failed payments
  - `CANCELLED` - Cancelled payments
  - `REFUNDED` - Refunded payments

**Examples:**
```javascript
// Get all unpaid payments
GET /api/payments/user/filtered?status=PENDING

// Get paid and failed payments
GET /api/payments/user/filtered?status=PAID&status=FAILED

// Get all payments (no filter)
GET /api/payments/user/filtered
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "User payments retrieved successfully",
  "data": [
    {
      "id": "string",
      "userId": "string",
      "userFullName": "string",
      "userEmail": "string",
      "amount": "number",
      "method": "string",
      "status": "PENDING|PAID|FAILED|CANCELLED|REFUNDED",
      "description": "string",
      "transactionId": "string",
      "createdAt": "string",
      "updatedAt": "string"
    }
  ]
}
```

#### Get User's Payment Summary
```http
GET /api/payments/user/summary
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "User payment summary retrieved successfully",
  "data": {
    "totalPayments": "number",
    "pendingCount": "number",
    "completedCount": "number",
    "failedCount": "number",
    "cancelledCount": "number",
    "refundedCount": "number",
    "totalPaid": "number",
    "totalPending": "number",
    "unpaidCount": "number"
  }
}
```

### 2. Group Payment Filtering (For Admins/Moderators)

#### Get Group Payments with Status Filtering
```http
GET /api/payments/group/{groupId}/filtered?status=PENDING&status=PAID&status=FAILED
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `groupId` (required): ID of the group

**Query Parameters:**
- `status` (optional): List of payment statuses to filter by

**Examples:**
```javascript
// Get all unpaid payments in group
GET /api/payments/group/group123/filtered?status=PENDING

// Get paid and failed payments in group
GET /api/payments/group/group123/filtered?status=PAID&status=FAILED

// Get all payments in group (no filter)
GET /api/payments/group/group123/filtered
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Group payments retrieved successfully",
  "data": [
    {
      "id": "string",
      "userId": "string",
      "userFullName": "string",
      "userEmail": "string",
      "amount": "number",
      "method": "string",
      "status": "PENDING|PAID|FAILED|CANCELLED|REFUNDED",
      "description": "string",
      "transactionId": "string",
      "createdAt": "string",
      "updatedAt": "string"
    }
  ]
}
```

#### Get Group Payment Summary
```http
GET /api/payments/group/{groupId}/summary
```

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `groupId` (required): ID of the group

**Success Response (200):**
```json
{
  "success": true,
  "message": "Group payment summary retrieved successfully",
  "data": {
    "groupId": "string",
    "groupName": "string",
    "totalPayments": "number",
    "pendingCount": "number",
    "completedCount": "number",
    "failedCount": "number",
    "cancelledCount": "number",
    "refundedCount": "number",
    "totalPaid": "number",
    "totalPending": "number",
    "totalFailed": "number",
    "unpaidCount": "number",
    "paidCount": "number"
  }
}
```

## Frontend Implementation Examples

### For Members (Non-Admins)

#### Get User's Unpaid Payments
```javascript
const getUserUnpaidPayments = async () => {
  try {
    const response = await fetch('/api/payments/user/filtered?status=PENDING', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.success) {
      return result.data; // Array of unpaid payments
    }
  } catch (error) {
    console.error('Failed to get unpaid payments:', error);
  }
};
```

#### Get User's Payment Summary
```javascript
const getUserPaymentSummary = async () => {
  try {
    const response = await fetch('/api/payments/user/summary', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.success) {
      const summary = result.data;
      console.log(`You have ${summary.unpaidCount} unpaid payments`);
      console.log(`You have paid ${summary.totalPaid} total`);
      return summary;
    }
  } catch (error) {
    console.error('Failed to get payment summary:', error);
  }
};
```

### For Group Admins/Moderators

#### Get Group's Unpaid Payments
```javascript
const getGroupUnpaidPayments = async (groupId) => {
  try {
    const response = await fetch(`/api/payments/group/${groupId}/filtered?status=PENDING`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.success) {
      return result.data; // Array of unpaid payments in group
    }
  } catch (error) {
    console.error('Failed to get group unpaid payments:', error);
  }
};
```

#### Get Group Payment Summary
```javascript
const getGroupPaymentSummary = async (groupId) => {
  try {
    const response = await fetch(`/api/payments/group/${groupId}/summary`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.success) {
      const summary = result.data;
      console.log(`Group has ${summary.unpaidCount} unpaid payments`);
      console.log(`Group has collected ${summary.totalPaid} total`);
      return summary;
    }
  } catch (error) {
    console.error('Failed to get group payment summary:', error);
  }
};
```

#### Get Multiple Status Payments
```javascript
const getGroupPaidAndFailedPayments = async (groupId) => {
  try {
    const response = await fetch(`/api/payments/group/${groupId}/filtered?status=PAID&status=FAILED`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.success) {
      return result.data; // Array of paid and failed payments
    }
  } catch (error) {
    console.error('Failed to get group payments:', error);
  }
};
```

## Error Handling

### HTTP Status Codes
- **200** - Success
- **401** - Unauthorized (invalid/missing token)
- **403** - Forbidden (not admin/moderator for group endpoints)
- **404** - Group not found

### Error Response Format
```json
{
  "success": false,
  "message": "Only admins and moderators can view group payment details",
  "error": "UNAUTHORIZED"
}
```

## Use Cases

### For Members
1. **View unpaid payments:** `GET /api/payments/user/filtered?status=PENDING`
2. **View payment history:** `GET /api/payments/user/filtered?status=PAID`
3. **Get payment summary:** `GET /api/payments/user/summary`

### For Group Admins
1. **View all unpaid payments:** `GET /api/payments/group/{groupId}/filtered?status=PENDING`
2. **View payment statistics:** `GET /api/payments/group/{groupId}/summary`
3. **View failed payments:** `GET /api/payments/group/{groupId}/filtered?status=FAILED`
4. **View all payments:** `GET /api/payments/group/{groupId}/filtered`

## Testing Checklist

### For Members
- [ ] Test getting unpaid payments
- [ ] Test getting paid payments
- [ ] Test getting payment summary
- [ ] Test getting all payments (no filter)

### For Group Admins
- [ ] Test getting group unpaid payments
- [ ] Test getting group payment summary
- [ ] Test getting multiple status payments
- [ ] Test permission validation (non-admin users)

### Error Scenarios
- [ ] Test with invalid token
- [ ] Test with non-admin user accessing group endpoints
- [ ] Test with non-existent group ID
- [ ] Test with invalid status values

## Summary

This new functionality provides:
- **Clear paid/unpaid distinction** for better UX
- **Status filtering** for both members and admins
- **Payment summaries** with counts and totals
- **Permission-based access** (admins only for group data)
- **Flexible querying** with multiple status support

The implementation is clean, backward-compatible, and provides all the functionality needed for both member and admin payment management!
