package com.evaluate.ai.langchain.rag;

import com.evaluate.ai.langchain.model.CustomMetadata;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    private final int MAX_RESULTS = 3;
    private final double MIN_SCORE = 0.5;

    public RagService(DataSource dataSource, EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("document_embeddings")
                .dimension(embeddingModel.dimension())  // 384 for AllMiniLmL6V2
                .createTable(true)
                //.searchMode(PgVectorEmbeddingStore.SearchMode.HYBRID)  // Enable hybrid search (default: SearchMode.VECTOR)
                //.textSearchConfig("english")    // Optional: PostgreSQL text search config (default: "simple")
                //.rrfK(60)
                .build();
    }


    public void embedDocsInPgVector(Document document, CustomMetadata customMetadata) {
        log.info("Embedding documents in PgVector...");
        Metadata metadata = new Metadata();
        metadata.put("category", customMetadata.category());
        metadata.put("documentName", customMetadata.documentName());
        metadata.put("language", customMetadata.language());
        metadata.put("tags", customMetadata.tags());

        DocumentSplitter splitter = DocumentSplitters.recursive(512, 60);
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        embeddingStoreIngestor.ingest(Document.document(document.text(), metadata));
        log.info("Embedding documents in PgVector complete!");
    }

    public String searchDocuments(String query) {
        log.info("Searching for: {}", query);

        Embedding questionEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .query(query)
                .maxResults(MAX_RESULTS)  // Retrieve top 3 most similar chunks
                .minScore(MIN_SCORE)  // Optional: set a minimum similarity score threshold
                .build();

        List<EmbeddingMatch<TextSegment>> relevantSegments = embeddingStore.search(searchRequest).matches();
        String context = relevantSegments.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n"));
        return context.isEmpty() ? "" : context;
    }

    public ContentRetriever embeddingStoreContentRetriever() {
        Filter onlyTopic = metadataKey("tags").containsString("Java");

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();
    }
}
