//Student Grade Tracker Build a Java program to input and manage student grades. 
//Calculate average, highest, and lowest scores. 
//Use arrays or ArrayLists to store and manage data. 
//Display a summary report of all students.
import java.util.ArrayList;
import java.util.Scanner;

class Student {
    String name;
    ArrayList<Double> grades;

    Student(String name) {
        this.name = name;
        this.grades = new ArrayList<>();
    }

    void addGrade(double grade) {
        grades.add(grade);
    }

    double getAverage() {
        if (grades.isEmpty()) return 0.0;
        double sum = 0;
        for (double g : grades) sum += g;
        return sum / grades.size();
    }

    double getHighest() {
        if (grades.isEmpty()) return 0.0;
        double max = grades.get(0);
        for (double g : grades) {
            if (g > max) max = g;
        }
        return max;
    }

    double getLowest() {
        if (grades.isEmpty()) return 0.0;
        double min = grades.get(0);
        for (double g : grades) {
            if (g < min) min = g;
        }
        return min;
    }
}

public class StudentGradeTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Student> students = new ArrayList<>();

        System.out.print("Enter number of students: ");
        int numStudents = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        for (int i = 0; i < numStudents; i++) {
            System.out.print("Enter name of student " + (i + 1) + ": ");
            String name = scanner.nextLine();
            Student s = new Student(name);
            
            System.out.print("Enter number of grades for " + name + ": ");
            int numGrades = scanner.nextInt();
            
            for (int j = 0; j < numGrades; j++) {
                System.out.print("Enter grade " + (j + 1) + ": ");
                double grade = scanner.nextDouble();
                s.addGrade(grade);
            }
            scanner.nextLine(); // consume newline
            students.add(s);
        }

        // Calculate overall highest and lowest grades (among all students)
        double overallHighest = Double.MIN_VALUE;
        double overallLowest = Double.MAX_VALUE;
        double totalSum = 0;
        int totalGradesCount = 0;

        System.out.println("\n----- Student Summary Report -----");
        for (Student s : students) {
            double avg = s.getAverage();
            double highest = s.getHighest();
            double lowest = s.getLowest();

            System.out.printf("Student: %s, Average Grade: %.2f, Highest: %.2f, Lowest: %.2f%n",
                              s.name, avg, highest, lowest);

            if (highest > overallHighest) overallHighest = highest;
            if (lowest < overallLowest) overallLowest = lowest;

            totalSum += avg * s.grades.size();
            totalGradesCount += s.grades.size();
        }

        double overallAverage = totalGradesCount > 0 ? totalSum / totalGradesCount : 0;

        System.out.println("\nOverall Highest Grade: " + overallHighest);
        System.out.println("Overall Lowest Grade: " + overallLowest);
        System.out.printf("Overall Average Grade: %.2f%n", overallAverage);

        scanner.close();
    }
}
