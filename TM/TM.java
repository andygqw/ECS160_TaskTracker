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
            logger.log(args[0]);

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

    // log element size
    protected static final int TASK_ATTR_SIZE = 5;
}

// Logger
// job: uses singleton design to read & write logs
class Logger{

    // Class instance
    private static Logger instance;
    
    private List<String> operationLog = new ArrayList<>();
    private List<Task> taskSummary = new ArrayList<>();

    // Private constructor
    private Logger(){

        try{

            File file = new File(Constants.LOG_FNAME);

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
            writer.write("Operation Log:\n");
            writer.write("\n"); // Empty content for Operation Log
            writer.write("Task Summary:\n");
            writer.write("\n"); // Empty content for Task Summary
        }
    }

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
                
                isValidLog++;
                isOpLog = false;
                isTaskSummary = true;
                continue;
            }

            if (isOpLog && !line.trim().isEmpty()){

                readOp(line);
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

    private void readOp(String line){

        operationLog.add(line);
    }

    private void readTask(String line){

        String[] words = line.split("\\s+");

        if (words.length != Constants.TASK_ATTR_SIZE){

            throw new RuntimeException("Invalid Task Summary");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

        ZonedDateTime startTime = ZonedDateTime.parse(words[2], formatter);
        ZonedDateTime endTime = ZonedDateTime.parse(words[3], formatter);

        Task task = new Task(words[0], words[1], startTime, endTime, words[4]);
        taskSummary.add(task);
    }

    // This write lines to log file
    protected void log(String msg){


    }

    protected List<String> getOperationLog(){

        return operationLog;
    }

    protected static Logger getInstance() {
        
        if (instance == null) {

            instance = new Logger();
        }

        return instance;
    }
}

class Task{

    private String taskName;
    private String taskSize;
    private ZonedDateTime taskStart;
    private ZonedDateTime taskEnd;
    private String taskDes;

    protected Task(String name, String size, ZonedDateTime start, ZonedDateTime end, String des){

        taskName = name;
        taskSize = size;
        taskStart = start;
        taskEnd = end;
        taskDes = des;
    }
}