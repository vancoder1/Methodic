# Methodic: Your Smart Task Manager 🚀

Methodic is a modern, intuitive, and efficient Android application designed to help you take control of your tasks and boost your productivity. Built with the latest Android development technologies, Methodic provides a seamless experience for managing your daily to-dos, projects, and goals.

## ✨ Key Features

*   **Comprehensive Task Management:** Create, view, update, and delete tasks with ease. Each task can include a title, detailed description, priority level, and due date.
*   **Flexible Prioritization System:** Assign one of three priority levels (High ⬆️, Medium ➡️, Low ⬇️) to your tasks, allowing you to quickly identify and focus on your most important items.
*   **Intelligent Filtering Options:** Effortlessly filter your task list by completion status (show all or hide completed) or by priority level (High, Medium, Low, or all) to streamline your view and find specific tasks quickly. 🔍
*   **Completion Tracking with Undo:** Mark tasks as complete with a simple swipe. If you make a mistake, an undo option is available via a Snackbar. ✅
*   **Due Date Management:** Set specific due dates for your tasks to help you stay on schedule and meet deadlines. 📅
*   **Clean and Responsive User Interface:** Developed using Jetpack Compose, Methodic offers a modern, smooth, and visually appealing user interface that adapts to different screen sizes. ✨
*   **Persistent Data Storage:** Task data is securely stored locally using the Room Persistence Library, ensuring your information is always available.

## 🛠️ Technologies and Architecture

Methodic is built using a modern Android development stack, following best practices for a robust and maintainable application:

*   **Kotlin:** The primary programming language, known for its conciseness, safety, and interoperability with Java.
*   **Android Jetpack Compose:** Android's declarative UI toolkit, significantly simplifying UI development.
*   **Android Architecture Components:**
    *   **ViewModel:** Manages UI-related data in a lifecycle-aware way, surviving configuration changes.
    *   **Room Persistence Library:** An abstraction layer over SQLite, providing a type-safe way to access the app's database.
    *   **Flow:** Utilized for asynchronous data streams, particularly for observing changes in the task database.
*   **Dependency Injection (Implicit):** Leveraging ViewModel factories for basic dependency provision.

## 🚀 Getting Started

To build and run Methodic on your local development environment, follow these steps:

1.  **Prerequisites:**
    *   Android Studio installed.
    *   An Android emulator or a physical Android device connected.
2.  **Clone the Repository:**
    Open your terminal or command prompt and clone the project:
    ```bash
    git clone https://github.com/vancoder1/Methodic.git
    ```
3.  **Open in Android Studio:**
    Launch Android Studio and open the cloned project directory.
4.  **Build and Run:**
    Sync the project with Gradle files, then build and run the application on your chosen emulator or device.

## 📱 How to Use

*   **Adding a Task:** Tap the **"+"** floating action button located at the bottom right of the screen. Fill in the task details in the modal sheet that appears and tap "Add Task".
*   **Marking as Complete/Deleting:** On the main task list, swipe left or right on a task item. Swiping one direction will mark it as complete, and the other will reveal a delete option.
*   **Filtering Tasks:** Tap the filter icon (looks like a funnel) in the top app bar. Select a priority filter or toggle the "Show Completed" option from the dropdown menu.
*   **Viewing Task Details:** Tap on a task item to view or edit its full details.

## 📸 Screenshots

<img src="https://github.com/user-attachments/assets/6d0cea97-8b2d-4e33-be79-6f12e5136bca" alt="Image 1" width="250px"/>
&nbsp; &nbsp; &nbsp;
<img src="https://github.com/user-attachments/assets/1494cf4c-4050-40f0-b8ee-47344469cdc6" alt="Image 2" width="250px"/>
&nbsp; &nbsp; &nbsp;
<img src="https://github.com/user-attachments/assets/b490abf6-74b9-4406-ba20-a8b1e5657e1f" alt="Image 3" width="250px"/>


## 🛣️ Future Enhancements (Ideas)

*   Adding support for task tags for better categorization.
*   Implementing recurring tasks.
*   Adding notifications for due dates.
*   Syncing tasks across devices.
*   Implementing search functionality.
*   Adding AI to sort tasks and assign difficulty automatically

## 📄 License

This project is licensed under the terms of the [LICENSE](LICENSE) file.

## 👋 Contributing

We welcome contributions to Methodic! If you are interested in contributing, be free to submit pull requests, report issues, and more.

---

Made with ❤️ by [vancoder1](https://github.com/vancoder1)
