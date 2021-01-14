package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CommandParser {

    @Parameter(names = {"-t"})
    public String type = null;
    @Parameter(names = {"-k"})
    public String key = null;
    @Parameter(names = {"-v"})
    public String value = null;
    @Parameter(names = {"-in"})
    public String filePath = null;

    public String parseCmdArgs(String[] args) {
        JCommander.newBuilder()
                .addObject(this)
                .build()
                .parse(args);
        return this.getCommand();
    }

    public String getCommand() {
        Gson gson = new Gson();
        StringBuilder json = new StringBuilder();

        if (this.filePath != null) {
            try {
                json.append(this.readFile(this.filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            json.append(gson.toJson(this));
        }
        return json.toString();
    }


    public String readFile(String fileName) throws IOException {
        String dir = System.getProperty("user.dir") + "\\src\\client\\data\\";
        String fullFilePath = dir + fileName;
        System.out.println("File Path to read :: " + fullFilePath);
        return new String(Files.readAllBytes(Paths.get(fullFilePath)));
    }
}

