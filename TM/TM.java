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
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import java.util.Map;


// Main body
public class TM{

    // Main function takes command line arguments and then process
    public static void main(String[] args){
        
        try{
            // Throw empty argument exception
            if (args.length == 0){

                throw new IllegalArgumentException("No command line arguments provided");
            }

            Logger logger = Logger.getInstance();

            // Parse arguments
            switch (args[0].toLowerCase()){
                case Constants.START:
                    
                    if (args.length != 2){

                        throw new IllegalArgumentException(Constants.START 
                                                + ": " + Constants.ERR_ARGUMENT);
                    }else{

                        if (args[1].length() > 22){

                            throw new IllegalArgumentException(Constants.ERR_EXCEED);
                        }
                        logger.startTask(args[1]);
                    }
                    break;

                case Constants.STOP:
                    
                    if (args.length != 2){

                        throw new IllegalArgumentException(Constants.STOP
                                                + ": "  + Constants.ERR_ARGUMENT);
                    }else{

                        logger.stopTask(args[1]);
                    }
                    break;

                case Constants.DESCRIBE:

                    if (args.length == 3){

                        logger.describeTask(args[1], args[2], Constants.UNDEFINED);
                    }else if(args.length == 4){
                        
                        logger.describeTask(args[1], args[2], args[3].toUpperCase());
                    }else{
                        throw new IllegalArgumentException(Constants.DESCRIBE 
                                                + ": " + Constants.ERR_ARGUMENT);
                    }

                    break;

                case Constants.SUMMARY:

                    if (args.length == 1){

                        logger.summaryTask();
                    }else if (args.length == 2){

                        if (logger.nameRestrict(args[1].toUpperCase())){

                            logger.summaryTask(TASK_SIZE.valueOf(args[1].toUpperCase()));
                        }else{
                            logger.summaryTask(args[1]);
                        }
                    }
                    break;

                case Constants.SIZE:

                    if (args.length == 3){

                        logger.sizeTask(args[1], args[2].toUpperCase());
                    }else{
                        throw new IllegalArgumentException(Constants.SIZE 
                                                + ": " + Constants.ERR_ARGUMENT);
                    }
                    break;

                case Constants.RENAME:

                    if (args.length == 3){

                        logger.renameTask(args[1], args[2]);

                    }else{
                        throw new IllegalArgumentException(Constants.RENAME
                                                + ": " + Constants.ERR_ARGUMENT);
                    }
                    break;
                default:
                    throw new IllegalArgumentException(Constants.ERR_ARGUMENT);
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

    // DateTime formate
    protected static final DateTimeFormatter FORMATTER 
    = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");

    // Minimum time which used to compare
    protected static final ZonedDateTime MIN_TIME 
    = ZonedDateTime.parse("2000/01/01-00:00:00", FORMATTER.withZone(ZoneId.systemDefault()));;

    // Printing tasks formats
    protected static final String PRINT_FORMAT = "%-22s";
    protected static final int PRINT_GAP = 22;
    protected static final String LABEL = 
                            String.format(Constants.PRINT_FORMAT, "Task Name")
                                + String.format(Constants.PRINT_FORMAT, "Task Size")
                                + String.format(Constants.PRINT_FORMAT, "Start Time")
                                + String.format(Constants.PRINT_FORMAT, "End Time")
                                + String.format(Constants.PRINT_FORMAT, "Description");

    protected static final String SUM_LABEL = 
                            String.format(Constants.PRINT_FORMAT, "Task Name")
                                + String.format(Constants.PRINT_FORMAT, "Time Spent");

    protected static final String UNDEFINED = "UNDEFINED";

    // Printing log formats
    protected static final String START = "start";
    protected static final String STOP = "stop";
    protected static final String DESCRIBE = "describe";
    protected static final String SUMMARY = "summary";
    protected static final String SIZE = "size";
    protected static final String RENAME = "rename";

    // Error messages
    protected static final String ERR_ARGUMENT = "Invalid command line argument";
    protected static final String ERR_NOT_RUNNING = "Task is not running";
    protected static final String ERR_TASK_RUNNING = "Task is running";
    protected static final String ERR_EXCEED= "Task Name exceeds " 
                                    + PRINT_GAP + " Characters";

}

// Size values
enum TASK_SIZE{
    UNDEFINED, S, M, L ,XL
}

// Data structure of a task
class Task{

    private String taskName;
    private TASK_SIZE taskSize;
    private ZonedDateTime taskStart;
    private ZonedDateTime taskEnd;
    private String taskDes;

    protected Task(String name){

        taskName = name;
        taskSize = TASK_SIZE.UNDEFINED;
        taskStart = ZonedDateTime.now();
        taskEnd = Constants.MIN_TIME;
        taskDes = Constants.UNDEFINED;
    }

    protected Task(String name, TASK_SIZE size, ZonedDateTime start, ZonedDateTime end, String des){

        taskName = name;
        taskSize = size;
        taskStart = start;
        taskEnd = end;
        taskDes = des;
    }

    // Check if name exists
    protected Predicate<String> hasTask = name -> taskName.equals(name);

    // Check if this task is still going
    protected Supplier<Integer> isRunning = () -> taskEnd.compareTo(Constants.MIN_TIME);

    // Stop this task
    protected void stop(){ taskEnd = ZonedDateTime.now(); }

    // Describle this task
    protected void describe(String description, TASK_SIZE size){

        taskDes = description;
        taskSize = size;
    }

    // Size this task
    protected void size(TASK_SIZE size){ taskSize = size; }

    // Rename this task
    protected void rename(String name){ taskName = name; }

    // Summarize a task
    protected Supplier<String> getName = () -> taskName;
    protected Predicate<TASK_SIZE> isSize = size -> taskSize.equals(size);

    protected Duration summaryTime(){

        if (taskEnd.compareTo(Constants.MIN_TIME) == 0){

            return Duration.between(taskStart, ZonedDateTime.now());
        }
        return Duration.between(taskStart, taskEnd);
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

    // Store cleaned tasks
    Map<String, Duration> map = new HashMap<>();

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

        // Initial clean data
        for (Task task : taskSummary){

            if (map.containsKey(task.getName.get())){

                Duration time = map.get(task.getName.get())
                                    .plus(task.summaryTime());

                map.put(task.getName.get(), time);
            }else{
                map.put(task.getName.get(), task.summaryTime());
            }
        }
    }

    // Helper function to read all tasks from log
    private void readTask(String line){

        int len = line.length();

        List<String> segments = new ArrayList<>();

        for (int i = 0; i < len; i += Constants.PRINT_GAP){

            int end = Math.min(i + Constants.PRINT_GAP, len);

            // Extract the substring
            String segment = line.substring(i, end);

            segment = segment.replaceAll("\\s+$", "");

            // Add the segment to the list
            segments.add(segment);
        }

        ZonedDateTime startTime = ZonedDateTime.parse(segments.get(2), 
                                    Constants.FORMATTER.withZone(ZoneId.systemDefault()));
        ZonedDateTime endTime = ZonedDateTime.parse(segments.get(3), 
                                    Constants.FORMATTER.withZone(ZoneId.systemDefault()));

        Task task = new Task(segments.get(0), 
                                TASK_SIZE.valueOf(segments.get(1)), 
                                    startTime, endTime, segments.get(4));
        taskSummary.add(task);
    }

    // Section2: Write logs to file
    private Task findTask(String name){

        Task result = null;

        // Find latest record
        for (Task task : taskSummary){

            if (task.hasTask.test(name)){

                result = task;
            }
        }

        return result; // Null if not found
    }

    // Name can't be S/M/L/XL or UNDEFINED
    // so this is a test
    protected boolean nameRestrict(String name){

        try {

            TASK_SIZE.valueOf(name.toUpperCase());

        } catch (IllegalArgumentException e) {
            
            return false;
        }

        return true;
    }
    // Operations:
    // Operate start
    protected void startTask(String name) throws IOException{

        if (nameRestrict(name)){

            throw new RuntimeException("""
                                        Invalid name. 
                                        It can't be one of the following: 
                                        S, M, L, XL, or UNDEFINED""");
        }
        Task target = findTask(name);

        if(target != null){

            if (target.isRunning.get() == 0){

                throw new RuntimeException(Constants.ERR_TASK_RUNNING);
            }
        }

        // Add task to our record
        target = new Task(name);
        taskSummary.add(target);

        // Print log message
        printLog(Constants.START, name);
    }

    // Operate Stop
    protected void stopTask(String name) throws IOException{

        Task target = findTask(name);

        if(target != null){

            if (target.isRunning.get() == 0){

                target.stop();
            }else{

                throw new RuntimeException(Constants.ERR_NOT_RUNNING); 
            }
        }else{

            throw new RuntimeException("Couldn't find " + name);
        }

        // Print log message
        printLog(Constants.STOP, name);

    }

    // Operate Describe
    protected void describeTask(String name, 
                                    String description, 
                                        String size) throws IOException{

        Task target = findTask(name);
        if (target == null){

            throw new RuntimeException("Couldn't find " + name);
        }
        
        for (Task task : taskSummary){

            if (task.hasTask.test(name)){

                try {

                    TASK_SIZE s = TASK_SIZE.valueOf(size);
                    task.describe(description, s);

                } catch (IllegalArgumentException e) {
                    
                    System.out.println("Invalid size: " + size);
                    System.exit(0);
                }
            }
        }
        printLog(Constants.DESCRIBE, name);
    }

    // Operate Size
    protected void sizeTask(String name, String size) throws IOException{
        
        Task target = findTask(name);
        if (target == null){

            throw new RuntimeException("Couldn't find " + name);
        }

        // Change size for every time window
        for (Task task : taskSummary){

            if (task.hasTask.test(name)){

                try {

                    TASK_SIZE s = TASK_SIZE.valueOf(size);
                    task.size(s);

                } catch (IllegalArgumentException e) {
                    
                    System.out.println("Invalid size: " + size);
                    System.exit(0);
                }
            }
        }
        printLog(Constants.SIZE, name);
    }

    // Operate Rename
    protected void renameTask(String name, String newName) throws IOException{

        Task target = findTask(name);
        if (target == null){

            throw new RuntimeException("Couldn't find " + name);
        }

        for (Map.Entry<String, Duration> entry : map.entrySet()){

            if (entry.getKey().equals(newName)){

                throw new RuntimeException("Name: " + newName + " already exists");
            }
        }

        // Change name for every time window
        for (Task task : taskSummary){

            if (task.hasTask.test(name)){

                task.rename(newName);
            }
        }
        printLog(Constants.RENAME, name);
    }

    // Operate Summary all
    // Using map to calculate multiple time windows 
    // with same name.
    protected void summaryTask(){

        // Print here
        System.out.println(Constants.SUM_LABEL);
        for (Map.Entry<String, Duration> entry : map.entrySet()){

            Duration timeDifference = entry.getValue();

            System.out.println(String.format(Constants.PRINT_FORMAT, entry.getKey())
                                + String.format(Constants.PRINT_FORMAT, 
                                    timeConverter(timeDifference)));
        }
    }
    // Operate summary with Task name argument
    protected void summaryTask(String name){

        Task target = findTask(name);

        if (target == null){

            throw new RuntimeException("Couldn't find " + name);
        }

        System.out.println(Constants.SUM_LABEL);

        System.out.println(String.format(Constants.PRINT_FORMAT, name)
                            + String.format(Constants.PRINT_FORMAT, 
                                timeConverter(map.get(name))));
    }
    // Operate summary with Size argument
    protected void summaryTask(TASK_SIZE size){

        List<String> viewed = new ArrayList<>();

        System.out.println(Constants.SUM_LABEL);

        for (Task task : taskSummary){

            if (task.isSize.test(size)){

                if (!viewed.contains(task.getName.get())){

                    System.out.println(String.format(Constants.PRINT_FORMAT, 
                                                        task.getName.get())
                    + String.format(Constants.PRINT_FORMAT, 
                                timeConverter(map.get(task.getName.get())))); 

                    viewed.add(task.getName.get());
                }               
            }
        }
    }

    private String timeConverter(Duration timeDifference){

        return timeDifference.toHours() + " Hours, " 
                        + timeDifference.toMinutesPart() + " Minutes, " 
                        + timeDifference.toSecondsPart() + " Seconds";
    }

    private void printHelper(String msg) throws IOException{

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

    private void printLog(String op, String name) throws IOException{

        String msg =  String.format(Constants.PRINT_FORMAT, op)
                + String.format(Constants.PRINT_FORMAT, name)
                + String.format(Constants.PRINT_FORMAT, 
                                (ZonedDateTime.now()).format(Constants.FORMATTER));

        printHelper(msg);
    }

    protected static Logger getInstance() {
        
        if (instance == null) {

            instance = new Logger();
        }

        return instance;
    }
}