package com.evaluate.ai.langchain.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@Slf4j
public class DocumentLoader {

    /**
     * Loads multiple documents from resources directory using glob pattern
     * Example: loadMultipleDocumentsWithGlob("/static/docs") will load all *.txt files from resources/static/docs
     */
    public static List<Document> getMultipleDocumentsWithGlob(String directory) {
        try {
            Path directoryPath = toPath(directory);
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.pdf");
            log.info("Loading *.pdf documents from: {}", directoryPath);
            List<Document> documents = loadDocuments(directoryPath, pathMatcher, new ApacheTikaDocumentParser());
            log.info("Successfully loaded {} document(s)", documents.size());
            documents.forEach(DocumentLoader::log);
            return documents;
        } catch (Exception e) {
            log.error("Error loading documents from directory: {}", directory, e);
            throw new RuntimeException("Failed to load documents from: " + directory, e);
        }
    }

    private static void log(Document document) {
        String fileName = document.metadata().getString("file_name");
        String preview = document.text().trim().length() > 50
                ? document.text().trim().substring(0, 50) + "..."
                : document.text().trim();
        log.info("{}: {}", fileName, preview);
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = DocumentLoader.class.getResource(fileName);
            if (fileUrl == null) {
                throw new RuntimeException("Resource not found: " + fileName +
                        ". Make sure the path starts with '/' for absolute resource paths (e.g., '/static/docs')");
            }
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for resource: " + fileName, e);
        }
    }
}
