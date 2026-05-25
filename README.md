# Running la Safor

Running la Safor is a comprehensive JavaFX desktop application designed to manage, analyze, and visualize sports activities. Originally built to provide an intuitive user interface for athletes, the application allows users to import GPX files, review their performance statistics, and manage custom map annotations. 

The application features a modern, clean, and responsive user interface with dynamic visual elements, floating labels, and glassmorphism-inspired design principles.

## Features

- **User Authentication**: Secure login and registration with robust validation for email, passwords, and user details.
- **Profile Management**: Update user information, including avatars, nicknames, emails, and passwords securely.
- **Activity Dashboard**: A centralized home view to visualize all imported sports activities with key metrics (distance, date, name).
- **GPX Import**: Easily import standard `.gpx` files to add new activities to your profile.
- **Detailed Activity View**: Dive deep into the statistics of a specific activity.
- **Map & Annotation Management**: Interactive map features where users can draw and save custom annotations (lines, text, points, and circles) with customizable colors and stroke widths.
- **Accumulated Statistics**: Track your progress over time with aggregated data views.
- **Session History**: Keep track of user login sessions for security and auditing purposes.

## Technologies Used

- **Java**: Core programming language.
- **JavaFX**: Used for building the rich graphical user interface.
- **FXML**: Defines the structure of the UI components.
- **CSS**: Custom styling for JavaFX (`nord-light` theme base with extensive custom overrides for a premium feel).
- **Ant**: Build tool used for compiling and packaging the NetBeans project.

## Prerequisites

To run and build this project, you will need:
- Java Development Kit (JDK) 11 or higher (with JavaFX support, or JavaFX SDK configured).
- Apache Ant (for command-line builds) or NetBeans IDE.

## Getting Started

### Using NetBeans IDE

1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com/yourusername/Running-la-Safor.git
   ```
2. Open NetBeans IDE.
3. Select **File > Open Project** and navigate to the cloned directory.
4. Right-click the project and select **Run** to launch the application.

### Using Command Line (Ant)

1. Navigate to the project directory:
   ```bash
   cd "Running-la-Safor"
   ```
2. Build the project:
   ```bash
   ant compile
   ```
3. Run the application:
   ```bash
   ant run
   ```

## Project Structure

- `src/application/`: Contains the main entry point `App.java`.
- `src/controllers/`: JavaFX controllers handling the logic and event listeners for each view.
- `src/view/`: FXML files defining the layout of the screens.
- `src/resources/`: Contains CSS stylesheets (`styles.css`, `nord-light.css`), images, and other assets used by the UI.
- `src/utils/`: Utility classes for navigation, alerts, and formatting.

## UI/UX Design Note

The user interface was crafted with a strong emphasis on modern design patterns. It features custom-styled fields with floating labels, elevated white cards over beautifully generated abstract backgrounds, and an overall layout that emphasizes usability and aesthetics.

## License

This project is intended for educational and personal use.