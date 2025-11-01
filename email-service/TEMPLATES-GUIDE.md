# Email Templates Guide

## Available Templates

### 1. **new-customer.html** - New Customer Welcome Email

Use this when a brand new customer registers with Credexa for the first time.

**Required Template Data:**

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("customerId", "CUST123456");
templateData.put("email", "john@example.com");
templateData.put("mobile", "+91 9876543210");
templateData.put("registrationDate", "October 21, 2025");
templateData.put("loginUrl", "https://credexa.com/login");
```

**Usage Example:**

```java
emailServiceClient.sendTemplatedEmail(
    "customer@example.com",
    "Welcome to Credexa Family!",
    "new-customer",
    templateData
);
```

---

### 2. **new-account.html** - New Account Opening Email

Use this when an existing customer opens a new account (FD, Savings, etc.).

**Required Template Data:**

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("accountNumber", "FD123456789");
templateData.put("accountType", "Fixed Deposit");
templateData.put("openingDate", "October 21, 2025");

// Optional fields for FD accounts
templateData.put("depositAmount", "50,000");
templateData.put("interestRate", "7.5");
templateData.put("maturityDate", "October 21, 2026");
templateData.put("maturityAmount", "53,750");
templateData.put("accountUrl", "https://credexa.com/accounts/FD123456789");
```

**Usage Example:**

```java
emailServiceClient.sendTemplatedEmail(
    "customer@example.com",
    "New Account Opened Successfully!",
    "new-account",
    templateData
);
```

---

### 3. **welcome.html** - Simple Welcome Email

Use this for basic welcome messages (simplified version).

**Required Template Data:**

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("name", "John Doe");
templateData.put("loginUrl", "https://credexa.com/login");
```

---

### 4. **reset-password.html** - Password Reset Email

Use this when a customer requests a password reset.

**Required Template Data:**

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("name", "John Doe");
templateData.put("otp", "123456");
templateData.put("expiryMinutes", "10");
```

---

### 5. **account-notification.html** - General Account Notifications

Use this for account updates, status changes, or general notifications.

**Required Template Data:**

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("message", "Your account has been updated successfully.");

// Optional account details
Map<String, String> accountDetails = new HashMap<>();
accountDetails.put("accountNumber", "FD123456789");
accountDetails.put("accountType", "Fixed Deposit");
accountDetails.put("status", "Active");
templateData.put("accountDetails", accountDetails);
```

---

## Logo Setup

### Step 1: Add Your Logo

Place your logo image file in:

```
email-service/src/main/resources/static/images/logo.png
```

**Recommended logo specifications:**

- Format: PNG (transparent background preferred)
- Size: 300x150px or similar aspect ratio
- Max width: 150px (will be auto-sized in emails)

### Step 2: Logo is Already Configured

All templates are pre-configured with the logo. The logo will appear at the top of every email with a gradient background.

---

## Integration Examples

### Example 1: Send New Customer Email (Customer Service)

```java
// In CustomerService.java after creating customer
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", customer.getFullName());
templateData.put("customerId", customer.getId().toString());
templateData.put("email", customer.getEmail());
templateData.put("mobile", customer.getMobileNumber());
templateData.put("registrationDate", LocalDate.now().toString());
templateData.put("loginUrl", "https://credexa.com/login");

emailServiceClient.sendTemplatedEmail(
    customer.getEmail(),
    "Welcome to Credexa Family!",
    "new-customer",
    templateData
);
```

### Example 2: Send New Account Email (FD Account Service)

```java
// In FDAccountService.java after creating FD account
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", fdAccount.getCustomerName());
templateData.put("accountNumber", fdAccount.getAccountNumber());
templateData.put("accountType", "Fixed Deposit");
templateData.put("openingDate", fdAccount.getCreatedDate().toString());
templateData.put("depositAmount", fdAccount.getDepositAmount().toString());
templateData.put("interestRate", fdAccount.getInterestRate().toString());
templateData.put("maturityDate", fdAccount.getMaturityDate().toString());
templateData.put("maturityAmount", fdAccount.getMaturityAmount().toString());
templateData.put("accountUrl", "https://credexa.com/accounts/" + fdAccount.getAccountNumber());

emailServiceClient.sendTemplatedEmail(
    fdAccount.getCustomerEmail(),
    "New Fixed Deposit Account Opened!",
    "new-account",
    templateData
);
```

---

## Template Features

### All Templates Include:

✅ **Professional Logo Header** - Gradient background with centered logo  
✅ **Responsive Design** - Works on all devices  
✅ **Modern Styling** - Clean, professional look  
✅ **Brand Colors** - Consistent Credexa branding  
✅ **Footer** - Contact info, links, copyright

### Template-Specific Features:

**new-customer.html:**

- Customer info box with all details
- Features list highlighting benefits
- Call-to-action button
- Professional welcome message

**new-account.html:**

- Account details in highlighted box
- Deposit amount showcase
- Maturity information (for FD accounts)
- Benefits list specific to account type
- View account button

---

## Testing Templates

### Method 1: Using Swagger

1. Start email service: `http://localhost:8085`
2. Go to: `http://localhost:8085/swagger-ui.html`
3. Use `/api/email/send` endpoint
4. Send test email with template data

### Method 2: Using Customer Service

1. Create a new customer via Customer Service API
2. Check the customer's email inbox
3. Verify the email was received with logo

---

## Troubleshooting

### Logo Not Showing?

1. Ensure logo file is in: `src/main/resources/static/images/logo.png`
2. Check filename matches exactly: `logo.png`
3. Restart the email service

### Template Variables Not Showing?

1. Verify all required template data fields are provided
2. Check field names match exactly (case-sensitive)
3. Review email service logs for errors

### Email Not Sending?

1. Check Azure Communication Services configuration
2. Verify recipient email is valid
3. Check email service logs
4. Ensure email service is running on port 8085

---

## Customization

To customize templates:

1. Edit the HTML files in `src/main/resources/templates/`
2. Modify colors, fonts, or layout as needed
3. Add new template variables using Thymeleaf syntax: `th:text="${variableName}"`
4. Test changes thoroughly

---

## Support

For questions or issues:

- Check email service logs
- Review Azure Communication Services dashboard
- Contact the development team

© 2025 Credexa. All rights reserved.
