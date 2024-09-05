# Smart Tracking Seal - CargoGuard

The Smart Tracking Seal is an innovative electronic device designed to enhance the efficiency and security of goods transportation in containers. This project integrates the device with a mobile app, allowing real-time tracking and management of cargo.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Device Specifications](#device-specifications)
- [Mobile App Details](#mobile-app-details)
- [Firebase Firestore Organization](#firebase-firestore-organization)
- [Usage Instructions](#usage-instructions)

## Project Overview

The Smart Tracking Seal project combines a physical electronic device with a mobile app to optimize the security and management of transported goods. The device, built with Atmega328P and A9G module, tracks real-time location and lock status, sending updates via SMS and storing data on Firebase Firestore. The accompanying Android app, developed in Java using Android Studio, allows users to manage cargo, track trucks, and receive notifications seamlessly.

## Features
- **Reusable Wire Seal**: The physical design incorporates a wire that acts as the seal, offering the benefits of reusability.
- **Firebase Integration**: Utilizes Firestore for real-time data storage and Firebase Authentication for user management.
- **Mobile App**: Developed with Android Studio, featuring a user-friendly interface for cargo management, truck tracking, and support.
- **Real-time Tracking**: Updates location and lock status regularly, ensuring up-to-date information for users.
- **Notifications**: Sends app notifications when lock status changes.
- **Support**: Offers FAQ and direct chat support via WhatsApp.

## Device Specifications
- **Microcontroller**: Atmega328P
- **Communication Module**: A9G
- **Battery**: Li-ion Battery BMS Charger Protection Board 3.7V 10A 3S
- **Functionality**: Enables real-time location tracking and lock state verification, with SMS notifications.


## Mobile App Details

The "CargoGuard" app was developed using Android Studio and Java. Firebase Firestore Database is used to store data, and Firebase Authentication handles user authentication.

### User Registration and Authentication
Users register through the app. Upon registration, a new document is created in the users collection in Firestore with fields for email and full name.

### Persistent Login
CargoGuard keeps users logged in until they choose to log out, similar to apps like Instagram and Facebook.

### Navigation
Users can navigate among Home, Cargo Management, Truck Tracking, and Support using the bottom navigation bar.

- ### Home
Provides a general description of the device and its key features.

- ### Cargo Management
Lists all locks registered under the user. Users can manage cargo details associated with each lock.

#### Steps:
1. **List View**: Displays locks (e.g., D001, D002, D003) registered under the user.
2. **Cargo Details Form**: Clicking on a lock directs to a form to store cargo details (Driver name, Truck name, Contents, Weight).
3. **Data Storage**: On form submission, a new collection (e.g., Journey1, Journey2) is created under the lock document in Firestore with the details stored in a `details` document.

- ### Truck Tracking
Allows users to track trucks locked with their registered locks.

#### Steps:
1. **List View**: Displays locks owned by the user.
2. **Truck Details**: Clicking on a lock shows truck details (Driver, Truck Number, Contents, Weight) and lock status.
3. **Real-time Tracking**: Users can view the current and previous locations on Google Maps, with real-time updates every minute.

- ### Support
Provides FAQs and facilitates direct chat with the assistant team via WhatsApp.

## Firebase Firestore Organization
### Firestore Structure
- **users** (Collection)
  - **User Document (e.g., U01, U02)**: Fields: email, full name
- **locks** (Collection)
  - **Lock Document (e.g., D001, D002)**
  - **journeys** (Subcollection)
  -  **Journey Document** (e.g., Journey1, Journey2))
    - **details** (Document): Stores journey-specific details
    - **locations** (Document): Stores location data with timestamp
   
    - users (Collection)
   ├── User Document (e.g., U01, U02)
        ├── Fields: email, full name
        ├── locks (Collection)
             ├── Lock Document (e.g., D001, D002)
                  ├── journeys (Subcollection)
                       ├── Journey Document (e.g., Journey1, Journey2)
                            ├── details (Document)
                                 ├── Fields: driverName, truckName, contents, weight, lockStatus
                            ├── locations (Collection)
                                 ├── Location Document (e.g., Location1, Location2)
                                      ├── Fields: latitude, longitude, timestamp

### Data Storage and Fetching
- **Registration**: User data is stored in the `users` collection.
- **Cargo Management**: Cargo details are stored in `journey_` subcollection under the specific lock document.
- **Truck Tracking**: Fetches data from `details` document and `locations` documents for real-time tracking.

## Usage Instructions
1. **Register**: Sign up through the CargoGuard app.
2. **Log In**: Use your credentials to log in.
3. **Cargo Management**: Manage and input cargo details for each lock.
4. **Truck Tracking**: Track the truck's real-time and past locations, Lock status and Details.
5. **Support**: Access FAQs or contact support via WhatsApp.
