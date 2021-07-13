package tech.stackable.t2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

public class SystemCommandRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemCommandRunnable.class);

    private final String command;
    private final List<String> commandWithArguments;
    private Path directory;

    private final Charset defaultCharset = UTF_8;

    private final Map<String, String> environment = new HashMap<>();

    private SystemCommandResult result = new SystemCommandResult(-1, null, null);

    public SystemCommandRunnable(String command, List<String> commandWithArguments) {
        this.command = command;
        this.commandWithArguments = commandWithArguments;
    }

    @Override
    public void run() {
        this.result = executeCommand(commandWithArguments);
    }

    private SystemCommandResult executeCommand(List<String> commandWithArguments) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commandWithArguments);
        if (directory != null) {
            builder.directory(directory.toFile());
        }
        builder.environment().putAll(environment);

        LOGGER.info("Running system command '{}'", commandWithArguments);
        try {
            Process process = builder.start();

            int exitCode = executeExternalCommand(process);

            String stdout = captureStandardOut(process);
            String stderr = captureStandardErr(process);
            if (!stderr.isEmpty()) {
                LOGGER.warn("Stderr output was:\n{}", stderr);
            }

            LOGGER.info("System command {} ran properly.", command);
            return new SystemCommandResult(exitCode, stdout, stderr);
        } catch (IOException e) {
            LOGGER.error("System command failed. Maybe command location '"
                    + commandWithArguments.get(0) + "' is wrong?!", e);
            return new SystemCommandResult(-1, "", e.getMessage());
        }
    }

    private int executeExternalCommand(Process process) throws IOException {
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("System command interrupted.", e);
            LOGGER.warn("Interrupted command output was:\n{}", captureStandardOut(process));
            LOGGER.warn("Interrupted command error was:\n{}", captureStandardErr(process));
            return -1;
        }
        return exitCode;
    }

    private String captureStandardOut(Process process) throws IOException {
        String stdOut;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), defaultCharset))) {
            stdOut = bufferedReader.lines().collect(Collectors.joining("\n"));
        }
        return stdOut;
    }

    private String captureStandardErr(Process process) throws IOException {
        String stdErr;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), defaultCharset))) {
            stdErr = bufferedReader.lines().collect(Collectors.joining("\n"));
        }
        return stdErr;
    }

    public Optional<SystemCommandResult> getExecutionResult() {
        return ofNullable(result);
    }

    public SystemCommandRunnable withDirectory(Path directory) {
        this.directory = directory;
        return this;
    }

    public SystemCommandRunnable withEnvironmentFromProperties(Properties properties) {
        properties.forEach((key, value) -> environment.put(String.format("TF_VAR_%s", key), value.toString()));
        return this;
    }

    public SystemCommandRunnable withEnvironment(String key, String value) {
        environment.put(key, value);
        return this;
    }
}
