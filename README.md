# Simple Transfer API
This project implements a RESTful API for transferring funds between accounts of two users. The transfers are transactional and synchronous, ensuring no concurrency issues (example of usage pessimistic lock, and pretend deadlock). The API includes a validation system that uses RabbitMQ to enforce transfer restrictions asynchronously.

## Features

- User Management: Manage users with attributes like ID, first name, last name, email, and a list of accounts.
- Account Management: Manage accounts with attributes like ID, number, type, balance, and a history of transactions.
- Synchronous Transfers: Perform fund transfers between user accounts transactionally and synchronously.
- Asynchronous Validation: Validate transfers against predefined rules using RabbitMQ.
- Transfer Restrictions: Enforce restrictions such as maximum transfer amount and no transfers on weekends.
