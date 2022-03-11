package info.kgeorgiy.ja.boguslavskaya.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    public static final Comparator<Student> STUDENT_COMPARATOR = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed().thenComparing(Student::compareTo);

    private <T> List<T> getStudentsPropertyInList(Collection<Student> students, Function<Student, T> function) {
        return students
                .stream()
                .map(function)
                .collect(Collectors.toList());
    }

    private <T> TreeSet<T> getStudentsPropertyInSet(List<Student> students, Function<Student, T> function) {
        return students
                .stream()
                .map(function)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentsPropertyInList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentsPropertyInList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getStudentsPropertyInList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentsPropertyInList(students, (Student st) -> st.getFirstName() + " " + st.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getStudentsPropertyInSet(students, Student::getFirstName);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students
                .stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sortStudentWithComparator(Collection<Student> students, Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentWithComparator(students, Comparator.comparing(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentWithComparator(students, STUDENT_COMPARATOR);
    }

    private Stream<Student> findByProperty(Collection<Student> students, Predicate<Student> function) {
        return students
                .stream()
                .filter(function)
                .sorted(Comparator.comparing(Student::getFirstName))
                .sorted(Comparator.comparing(Student::getLastName));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findByProperty(students, (student) -> student.getFirstName().equals(name))
                .sorted(Comparator.comparing(Student::getId))
                .sorted(STUDENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findByProperty(students, (student) -> student.getLastName().equals(name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findByProperty(students, (student) -> student.getGroup().equals(group))
                .sorted(Comparator.comparing(Student::getId))
                .sorted(STUDENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group)
                .stream()
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
}
