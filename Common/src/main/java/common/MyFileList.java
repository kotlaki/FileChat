package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

// возвращаем список файлов которые лежат в определенной папке
public class MyFileList {
    public static List<String> listFile(String path) throws IOException {
        return Files.list(Paths.get(path))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public static List<Long> sizeFile(String path) throws IOException {
        return Files.list(Paths.get(path))
                .map(p -> p.toFile().length())
                .collect(Collectors.toList());
    }

    public static List<Boolean> isDirectory(String path) throws IOException {
        return Files.list(Paths.get(path))
                .filter(p -> Files.isDirectory(p))
                .map(p -> p.toFile().isDirectory())
                .collect(Collectors.toList());
    }

    public static HashMap<List<String>, List<Long>> infoFile(String path) throws IOException {
        HashMap<List<String>, List<Long>> infoFile = new HashMap<>();
        infoFile.put(listFile(path), sizeFile(path));
        return infoFile;
    }


    public static void main(String[] args) throws IOException {
        new MyFileList();
        System.out.println(infoFile("client_storage"));
//        System.out.println(listFile("client_storage/"));
    }
}
