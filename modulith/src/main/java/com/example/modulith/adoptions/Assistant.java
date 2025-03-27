package com.example.modulith.adoptions;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Configuration
class Assistant {

    @Bean
    McpSyncClient mcpSyncClient() {
        var mcp = McpClient
                .sync(new HttpClientSseClientTransport("http://localhost:8081"))
                .build();
        mcp.initialize();
        return mcp;
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
                          McpSyncClient mcpClient,
                          DogRepository repository, VectorStore vectorStore) {
        if (false)
            repository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s"
                        .formatted(dog.id(), dog.name(), dog.description()));
                vectorStore.add(List.of(dogument));
            });

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Portland, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        return builder
                .defaultSystem(system)
                .defaultTools(new SyncMcpToolCallbackProvider(mcpClient))
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

}


@Controller
@ResponseBody
class AssistantController {

    private final ChatClient singularity;

    private final Map<String, PromptChatMemoryAdvisor> memory = new ConcurrentHashMap<>();

    AssistantController(ChatClient singularity) {
        this.singularity = singularity;
    }

    @PostMapping("/{user}/assistant")
    String assistant(@PathVariable String user, @RequestParam String question) {
        var advisor = this.memory
                .computeIfAbsent(user, _ -> PromptChatMemoryAdvisor.builder(new InMemoryChatMemory()).build());
        return this.singularity
                .prompt()
                .user(question)
                .advisors(advisor)
                .call()
                .content();
    }
}