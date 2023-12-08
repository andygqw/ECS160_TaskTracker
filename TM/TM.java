package TM;

// Imports
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;


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
            logger.log(null);

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
}

// Logger
// job: uses singleton design to read & write logs
class Logger{

    // Class instance
    private static Logger instance;
    
    private List<String> operationLog;
    private List<String> taskSummary;

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

            if (line.startsWith("Operation Log:")) {
                
                isValidLog++;
                isTaskSummary = false;
                isOpLog = true;
                continue;
            } else if (line.startsWith("Task Summary:")) {
                
                isValidLog++;
                isOpLog = false;
                isTaskSummary = true;
                continue;
            }

            if (isOpLog && line != "\n"){

                operationLog.add(line);
            }else if (isTaskSummary && line != "\n"){

                taskSummary.add(line);
            }
        }

        if (isValidLog != 2) {

                throw new RuntimeException("""
                                            Invalid Log file.
                                            A good log file should contain
                                            Operation Log and Task Summary""");
        }
    }

    protected void log(String msg){


    }

    protected static Logger getInstance() {
        
        if (instance == null) {

            instance = new Logger();
        }

        return instance;
    }
}

class Task{

    private int taskId;
    private String taskName;
    private String taskDes;
    private String taskSize;
}

class Operation{

    private int opId;
    private String opName;
    private ZonedDateTime opTime;

}