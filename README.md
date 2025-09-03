# Payment Gateway System

A robust Java-based payment gateway system with MySQL database integration, featuring secure transaction processing, comprehensive user account management, and multi-threaded transaction processing.

## Features

- **User Management**: Create users with multi-currency support  
- **Transaction Processing**: Secure deposits and withdrawals with queue-based processing  
- **Idempotency Protection**: Prevent duplicate transactions using unique keys  
- **Multi-threaded Processing**: Concurrent transaction handling with thread pool  
- **Database Integration**: MySQL with automatic schema creation  
- **Error Handling**: Robust exception management

## Setup

### 1. Create a MySQL Database

### 2. Configure Environment Variables
Set the following environment variables in your system:

- `DB_URL`  
- `DB_USER`  
- `DB_PASSWORD`

### 3. Execute Main.java file to run the project
