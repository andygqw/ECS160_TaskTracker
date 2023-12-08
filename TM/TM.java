package TM;

// Imports
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


// Main body
public class TM{

    // Main function takes command line arguments and then process
    public static void main(String[] args){
        
        try{
            // Throw empty argument exception
            if (args.length == 0){

                throw new Exception("No command line arguments provided");
            }

            Logger logger = Logger.getInstance();

            switch (args[0].toLowerCase()){
                case "start":
                    
                    if (args.length != 2){

                        throw new Exception("Invalid command line arguments");
                    }else{

                        logger.startTask(args[1]);
                    }
                    break;
                default:
                    throw new Exception("Invalid command line argument");
            }
        }catch(Exception ex){

            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }
}

// Constants class
class Constants{

    // Log file name
    protected static final String LOG_FNAME = "TM_log.txt";

    // log file section names
    protected static final String OP_LOG = "Operation Log:";
    protected static final String TASK_SUMMARY = "Task Summary:";

    // log element size (for error test)
    protected static final int TASK_ATTR_SIZE = 5;

    // DateTime formate
    protected static final DateTimeFormatter FORMATTER 
    = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");

    // Minimum time which used to compare
    protected static final ZonedDateTime MIN_TIME 
    = ZonedDateTime.parse("2000/01/01-00:00:00", FORMATTER.withZone(ZoneId.systemDefault()));;

    // Printing tasks formats
    protected static final String PRINT_FORMAT = "%-22s";
    protected static final String LABEL = 
                                    String.format(Constants.PRINT_FORMAT, "Task Name")
                                     + String.format(Constants.PRINT_FORMAT, "Task Size")
                                     + String.format(Constants.PRINT_FORMAT, "Start Time")
                                     + String.format(Constants.PRINT_FORMAT, "End Time")
                                     + String.format(Constants.PRINT_FORMAT, "Description");

    protected static final String UNDEFINED = "Undefined";

    // Printing log formats
    protected static final String START = "start";
}

// Data structure of a task
class Task{

    private String taskName;
    private String taskSize;
    private ZonedDateTime taskStart;
    private ZonedDateTime taskEnd;
    private String taskDes;

    protected Task(String name){

        taskName = name;
        taskSize = Constants.UNDEFINED;
        taskStart = ZonedDateTime.now();
        taskEnd = Constants.MIN_TIME;
        taskDes = Constants.UNDEFINED;
    }

    protected Task(String name, String size, ZonedDateTime start, ZonedDateTime end, String des){

        taskName = name;
        taskSize = size;
        taskStart = start;
        taskEnd = end;
        taskDes = des;
    }

    // Check if name exists
    protected boolean hasTask(String name){

        return taskName.equals(name);
    }

    // Check if this task is still going
    protected int isRunning(){

        return taskEnd.compareTo(Constants.MIN_TIME);
    }

    protected void startTask(String name){

        taskName = name;
        taskStart = ZonedDateTime.now();
    }

    // Format Task print results
    protected String printTask(){

        String result = "";
        result += String.format(Constants.PRINT_FORMAT, taskName);
        result += String.format(Constants.PRINT_FORMAT, taskSize);
        result += String.format(Constants.PRINT_FORMAT, taskStart.format(Constants.FORMATTER));
        result += String.format(Constants.PRINT_FORMAT, taskEnd.format(Constants.FORMATTER));
        result += String.format(Constants.PRINT_FORMAT, taskDes);

        return result;
    }
}

// Logger
// uses singleton design to read & write logs
class Logger{

    // Class instance
    private static Logger instance;
    
    private List<String> operationLog = new ArrayList<>();
    private List<Task> taskSummary = new ArrayList<>();

    // file obj
    private File file = new File(Constants.LOG_FNAME);

    // Private constructor
    private Logger(){

        try{

            if(!file.exists()){

                // Initialize log file
                createFile(file);
            }else{

                // Read lines
                readFile(file);
            }

        }catch(Exception ex){

            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }

    // Helper method to initialize log file content
    private static void createFile(File file) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(Constants.OP_LOG + "\n");
            writer.write("\n"); // Empty content for Operation Log
            writer.write(Constants.TASK_SUMMARY + "\n");
            writer.write(Constants.LABEL + "\n");
            writer.write("\n"); // Empty content for Task Summary
        }
    }

    // Section1: Read file into objects
    private void readFile(File file) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(file.toURI()));

        if (lines.size() < 2) {

            throw new RuntimeException("Invalid Log file");
        }

        // Test if log is valid
        int isValidLog = 0;

        boolean isOpLog = false;
        boolean isTaskSummary = false;

        for (String line : lines) {

            if (line.startsWith(Constants.OP_LOG)) {
                
                isValidLog++;
                isTaskSummary = false;
                isOpLog = true;
                continue;
            } else if (line.startsWith(Constants.TASK_SUMMARY)) {
                continue;
            } else if (line.equals(Constants.LABEL)){

                isValidLog++;
                isOpLog = false;
                isTaskSummary = true;
                continue;
            }

            if (isOpLog && !line.trim().isEmpty()){

                operationLog.add(line);
            }else if (isTaskSummary && !line.trim().isEmpty()){

                readTask(line);
            }
        }

        if (isValidLog != 2) {

                throw new RuntimeException("""
                                            Invalid Log file.
                                            A good log file should contain
                                            Operation Log and Task Summary""");
        }
    }

    // Helper function to read all tasks from log
    private void readTask(String line){

        String[] words = line.split("\\s+");

        if (words.length != Constants.TASK_ATTR_SIZE){

            throw new RuntimeException("Invalid Task Summary: members = " + words.length);
        }

        ZonedDateTime startTime = ZonedDateTime.parse(words[2], 
                                    Constants.FORMATTER.withZone(ZoneId.systemDefault()));
        ZonedDateTime endTime = ZonedDateTime.parse(words[3], 
                                    Constants.FORMATTER.withZone(ZoneId.systemDefault()));

        Task task = new Task(words[0], words[1], startTime, endTime, words[4]);
        taskSummary.add(task);
    }

    // Section2: Write logs to file
    private Task findTask(String name){

        Task result = null;

        // Find latest record
        for (Task task : taskSummary){

            if (task.hasTask(name)){

                result = task;
            }
        }

        return result; // Null if not found
    }

    // Operate start
    protected void startTask(String name) throws IOException{

        Task target = findTask(name);

        if(target != null){

            if (target.isRunning() == 0){

                throw new RuntimeException("Task is already running");
            }
        }

        // Add task to our record
        target = new Task(name);
        taskSummary.add(target);

        // Define log message
        String logMsg = Constants.START + " " + name + " " +
                            (ZonedDateTime.now()).format(Constants.FORMATTER);
        printLog(logMsg);
    }

    private void printLog(String msg) throws IOException{

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Operation Log:\n");
            for (String line : operationLog) {

                writer.write(line);
                writer.write("\n");
            }
            writer.write(msg + "\n");
            writer.write("\n");
            writer.write("Task Summary:\n");
            writer.write(Constants.LABEL + "\n");
            for (Task task : taskSummary) {

                writer.write(task.printTask());
                writer.write("\n");
            }
        }
    }

    protected static Logger getInstance() {
        
        if (instance == null) {

            instance = new Logger();
        }

        return instance;
    }
}