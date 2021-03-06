package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

// возвращаем список файлов которые лежат в определенной папке
public class MyFileList {
    public static List<String> listFile(String path) throws IOException {
        return Files.list(Paths.get(path))
                .filter(p->!Files.isDirectory(p))
                .map(p->p.getFileName().toString())
                .collect(Collectors.toList());
    }
}
