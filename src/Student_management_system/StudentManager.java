package Student_management_system;

import java.io.*;
import java.util.*;

/*
 * Manages student data persistence in students.txt.
 */
public class StudentManager {
    private static final String FILE_PATH = "students.txt";

    public static class Student {
        public String id;
        public String name;
        public String dept;
        public String sem;
        public String cgpa;
        public String phone;
        public String email;

        public Student(String id, String name, String dept, String sem, String cgpa, String phone, String email) {
            this.id = id;
            this.name = name;
            this.dept = dept;
            this.sem = sem;
            this.cgpa = cgpa;
            this.phone = phone;
            this.email = email;
        }

        // Format student details as CSV for file storage
        public String toFileString() {
            return id + "," + name + "," + dept + "," + sem + "," + cgpa + "," + phone + "," + email;
        }

        // Parse student object from CSV line
        public static Student fromFileString(String line) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 7) {
                return new Student(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
            }
            return null;
        }
    }

    static {
        createFileIfNotExists();
    }

    private static synchronized void createFileIfNotExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Read all student records from students.txt
    public static synchronized List<Student> getAllStudents() {
        createFileIfNotExists();
        List<Student> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Student s = Student.fromFileString(line);
                    if (s != null) {
                        students.add(s);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    // Append new student record to file
    public static synchronized boolean addStudent(String id, String name, String dept, String sem, String cgpa, String phone, String email) {
        Student s = new Student(id, name, dept, sem, cgpa, phone, email);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(s.toFileString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update existing student details by ID
    public static synchronized boolean updateStudent(String id, String name, String dept, String sem, String cgpa, String phone, String email) {
        if (id == null || id.trim().isEmpty()) return false;
        String cleanId = id.trim();
        List<Student> students = getAllStudents();
        boolean found = false;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).id.trim().equalsIgnoreCase(cleanId)) {
                students.set(i, new Student(cleanId, name.trim(), dept.trim(), sem.trim(), cgpa.trim(), phone.trim(), email.trim()));
                found = true;
                break;
            }
        }
        if (!found) {
            students.add(new Student(cleanId, name.trim(), dept.trim(), sem.trim(), cgpa.trim(), phone.trim(), email.trim()));
        }
        saveAllStudents(students);
        return true;
    }

    // Delete student record by ID
    public static synchronized boolean deleteStudent(String id) {
        List<Student> students = getAllStudents();
        boolean removed = students.removeIf(s -> s.id.equalsIgnoreCase(id));
        if (removed) {
            saveAllStudents(students);
        }
        return removed;
    }

    // Search students matching query keyword
    public static synchronized List<Student> searchStudents(String query) {
        List<Student> all = getAllStudents();
        if (query == null || query.trim().isEmpty() || query.trim().equalsIgnoreCase("Type an ID or Name")) {
            return all;
        }
        String q = query.trim().toLowerCase();
        List<Student> results = new ArrayList<>();
        for (Student s : all) {
            if (s.id.toLowerCase().contains(q) ||
                s.name.toLowerCase().contains(q) ||
                s.dept.toLowerCase().contains(q) ||
                s.sem.toLowerCase().contains(q) ||
                s.cgpa.toLowerCase().contains(q) ||
                s.phone.toLowerCase().contains(q) ||
                s.email.toLowerCase().contains(q)) {
                results.add(s);
            }
        }
        return results;
    }

    // Write all student records back to file
    private static synchronized void saveAllStudents(List<Student> students) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Student s : students) {
                writer.write(s.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Export student data to backup file
    public static synchronized boolean saveDataToFile(File destFile) {
        try (InputStream in = new FileInputStream(FILE_PATH);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Import student data from backup file
    public static synchronized boolean loadDataFromFile(File srcFile) {
        try (InputStream in = new FileInputStream(srcFile);
             OutputStream out = new FileOutputStream(FILE_PATH)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
