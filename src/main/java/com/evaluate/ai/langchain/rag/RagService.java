package com.evaluate.ai.langchain.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public RagService(DataSource dataSource) {
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.embeddingStore = PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("document_embeddings")
                .dimension(embeddingModel.dimension())  // 384 for AllMiniLmL6V2
                .createTable(true)
                .build();
    }


    public String embedDocsInPgVector(List<Document> documents) {
        log.info("Embedding documents in PgVector...");

        for (Document document : documents) {
            Embedding embedding = embeddingModel.embed(document.toTextSegment()).content();
            embeddingStore.add(embedding, document.toTextSegment());
        }

        Embedding queryEmbedding = embeddingModel.embed("What is new in spring batch?").content();

        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .build();

        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();

        EmbeddingMatch<TextSegment> embeddingMatch = relevant.getFirst();

        log.info("Score: {}", embeddingMatch.score());
        log.info("Match: {}", embeddingMatch.embedded().text());

        return "Successfully embedded documents in PgVector. Best match: " + embeddingMatch.embedded().text() + " (score: " + embeddingMatch.score() + ")";
    }

    public String searchDocuments(String query) {
        log.info("Searching for: {}", query);

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.6)
                .build();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(embeddingSearchRequest).matches();

        if (matches.isEmpty()) {
            return "No matches found for query: " + query;
        }

        StringBuilder result = new StringBuilder("Search results for: " + query + "\n");
        for (int i = 0; i < matches.size(); i++) {
            EmbeddingMatch<TextSegment> match = matches.get(i);
            result.append(String.format("%d. %s (score: %.4f)\n",
                    i + 1,
                    match.embedded().text(),
                    match.score()));
        }

        return result.toString();
    }
}
