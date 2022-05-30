package info.kgeorgiy.ja.boguslavskaya.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

/**
 * Class Implements {@link Impler} and {@link JarImpler}.
 * Provides methods to implement given interface and makes .java or .jar file with result class.
 *
 * @author Tatiana Boguslavskaya
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Constant for TAB.
     */
    private static final String TAB = " ".repeat(4); // :NOTE: four spaces

    /**
     * Constant for whitespace.
     */
    private static final String WB = " ";



    /**
     * Implements given interface and makes .jar file.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if gets problems with implementation or files.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            if (jarFile.getParent() != null){
                Files.createDirectories(jarFile.getParent());
            }
            Path tmpPath = Paths.get(".");
            implement(token, tmpPath);
            compileFiles(token, tmpPath);
            makeJarFile(token, jarFile);
        } catch (IOException e){
            throw new ImplerException("IOException", e);
        }
    }


    /**
     * Main function.
     * Implements given interface to .java file if 2 program arguments are given: {@code <token_to_be_implemented> <file_path>}.
     * Implements given interface to .jar file if 3 program arguments are given: {@code -jar <token_to_be_implemented> <file_path>}.
     * @param args - program arguments with information of given interface and file path.
     */
    public static void main(String[] args) {
        if (args == null || args.length > 3 || args.length < 2 || args[0] == null){
            System.out.println("Wrong arguments.");
        }
        Implementor implementor = new Implementor();
        assert args != null;
        try {
            if (args.length == 3 && args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (ClassNotFoundException | ImplerException e){
            System.err.println(e.getMessage());
        }
    }


    /**
     * Creates new .jar file and put given class to it.
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if problems with stream or files found.
     */
    private void makeJarFile(Class<?> token, Path jarFile) throws ImplerException{
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile))){
            String s = Paths.get(token.getPackageName().replace('.', File.separatorChar))
                    .resolve(token.getSimpleName().concat("Impl")).toString().replace(File.separatorChar, '/') + ".class";
            stream.putNextEntry(new JarEntry(s));
            Files.copy(Paths.get(s), stream);
        } catch (IOException e){
            throw new ImplerException("problems with jar output stream", e);
        }
    }

    /**
     * Compiles implementation of given interface.
     *
     * @param token type token to create implementation for.
     * @param path path where .java file needed to be compiled is.
     * @throws ImplerException if problems with compilation are found.
     */
    public static void compileFiles(final Class<?> token, final Path path) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String[] args = new String[]{"-cp",
                getClassPath(token),
                "-encoding",
                "UTF8",
                path.resolve(token.getPackage().getName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl" + ".java").toString() };
        if (compiler == null || compiler.run(null, null, null, args) != 0) {
          throw new ImplerException("Problems with compilation");
        }
    }


    /**
     * Gives a path to given token.
     *
     * @param token class which path is needed.
     * @return path of {@code token}.
     * @throws AssertionError when URISyntaxException is found.
     */
    private static String getClassPath(Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Implements given interface.
     *
     * Makes {@link String} with text made from {@link #getHeadOfClass(Class)} and puts it into given .java file.
     * @param token interface to create implementation for.
     * @param root root directory.
     * @throws ImplerException if gets problems with implementation or files.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can't implement this class");
        }
        root = makeResultFilePath(root, token);
        String res = getHeadOfClass(token) + getMethods(token) + "}";
        try (Writer writer = Files.newBufferedWriter(root)) {
            writer.write(res.chars().mapToObj(ch -> String.format("\\u%04X", ch)).collect(Collectors.joining()));
        } catch (IOException e) {
            throw new ImplerException("Problems with creating writer", e);
        }
    }

    /**
     * Creates directory for .java file.
     *
     * Replaces '.' to {@link File#separatorChar} and adds "Impl.java" and creates directory.
     * @param root path to file.
     * @param token interface needed to be implemented.
     * @return path for .java file.
     * @throws ImplerException if problems with creating directory are found.
     */
    private Path makeResultFilePath(Path root, Class<?> token) throws ImplerException {
        try {
            Path res = root.resolve(token.getPackageName().replace('.', File.separatorChar)).resolve(token.getSimpleName() + "Impl" + ".java");
            Path parent = res.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return res;
        } catch (IOException e) {
            // :NOTE: Add your own message - fixed
            throw new ImplerException("Problems with creating directory", e);
        }
    }

    /**
     * Makes part of code with package and class name.
     *
     * @param token interface needed to be implemented.
     * @return Part with package and class name in {@link String}.
     */
    private String getHeadOfClass(Class<?> token) {
        // :NOTE: empty package - fixed
        String pack = token.getPackageName();
        if (!pack.isEmpty()){
            pack = "package " + pack + ";" + System.lineSeparator() + System.lineSeparator();
        }
        return pack
                + "public class "
                + token.getSimpleName()
                + "Impl"
                + " implements "
                + token.getCanonicalName()
                + " {"
                + System.lineSeparator();
    }


    /**
     * Creates description of method's modifiers.
     *
     * Gives only {@code static} and {@code strict} modifiers, ignoring everyone else.
     * @param method method needed to get modifiers from.
     * @return Description of method's modifiers in {@link String}.
     */
    private String getModifiers(Method method) {
        StringBuilder res = new StringBuilder("public");
        int modifier = method.getModifiers();
        if (Modifier.isStatic(modifier)) {
            res.append(" static");
        }
        if (Modifier.isStrict(modifier)) {
            res.append(" strict");
        }
        return res.toString();
    }


    /**
     * Creates description of interface methods.
     *
     * Makes {@link String} with all interface's methods and their modifiers from {@link #getModifiers(Method)}.
     * @param token interface needed to be implemented.
     * @return description of given interface methods.
     */
    private String getMethods(Class<?> token) {
        Method[] methods = token.getMethods();
        StringBuilder res = new StringBuilder();
        for (Method method : methods) {
            if (method.isDefault()) {
                continue;
            }
            res.append(TAB)
                    .append(getModifiers(method))
                    .append(WB)
                    .append(method.getReturnType().getCanonicalName())
                    .append(WB)
                    .append(method.getName())
                    .append("(");
            Parameter[] parametrs = method.getParameters();
            int p = 0;
            for (Parameter parameter : parametrs) {
                res.append(p == 0 ? "" : "," + WB);
                res.append(parameter.getType().getCanonicalName())
                        .append(" ")
                        .append(parameter.getName());
                p++;
            }
            res.append(")")
                    .append(getExceptions(method))
                    .append("{")
                    .append(System.lineSeparator())
                    .append(TAB)
                    .append(TAB)
                    .append("return ")
                    .append(getDefaultValue(method))
                    .append(";")
                    .append(System.lineSeparator())
                    .append(TAB)
                    .append("}")
                    .append(System.lineSeparator());
        }
        return res.toString();
    }


    /**
     * Returns part with exceptions thrown by given method.
     * Exceptions are return as {@link String}.
     * @param method method needed to get exceptions from.
     * @return part with exceptions.
     */
    private String getExceptions(Method method) {
        StringBuilder res = new StringBuilder();
        if (method.getExceptionTypes().length != 0) {
            res.append(" throws ");
            Class<?>[] exceptions = method.getExceptionTypes();
            int p = 0;
            for (Class<?> exception : exceptions) {
                res.append(p == 0 ? "" : ", ");
                res.append(exception.getCanonicalName());
                p++;
            }
        }
        return res.toString();
    }


    /**
     * Returns default return value for given method.
     *
     * @param method method needed to get value for.
     * @return default return value for given method.
     */
    private String getDefaultValue(Method method) {
        // :NOTE: isPrimitive - fixed
        if (method.getReturnType().equals(boolean.class)){
            return "false";
        }
        if (method.getReturnType().equals(void.class)){
            return "";
        }
        if (method.getReturnType().isPrimitive()){
            return "0";
        }
        return "null";
    }

}
