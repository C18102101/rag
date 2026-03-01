/*
 * Copyright 2026-2027 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.alibaba.ai.example.agent.rag.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.cloud.alibaba.ai.example.agent.rag.tool.DocumentSearchTool;
import com.cloud.alibaba.ai.example.agent.rag.tool.KnowledgeRetrievalTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * Configuration for the RAG Agent
 * <p>
 * This configuration sets up a ReactAgent with knowledge retrieval capabilities,
 * implementing the Agentic RAG pattern. The agent can autonomously decide when
 * to query the knowledge base and how to synthesize responses.
 * </p>
 *
 * @author zth9
 * @since 2026-01-22
 */
@Configuration
public class RagAgentConfiguration {

	private final ChatModel chatModel;

	private final KnowledgeRetrievalTool knowledgeRetrievalTool;

	private final VectorStore vectorStore;

	public RagAgentConfiguration(ChatModel chatModel, KnowledgeRetrievalTool knowledgeRetrievalTool, VectorStore vectorStore) {
		this.chatModel = chatModel;
		this.knowledgeRetrievalTool = knowledgeRetrievalTool;
		this.vectorStore = vectorStore;
	}

	@Bean
	public ReactAgent ragAgent() throws GraphStateException {
		DocumentSearchTool searchTool = new DocumentSearchTool(vectorStore);

        ToolCallback searchCallback = FunctionToolCallback.builder("search_documents",
                        (Function<DocumentSearchTool.Request, DocumentSearchTool.Response>)
                                searchTool::search)
                .description("搜索文档以查找相关信息")
                .inputType(DocumentSearchTool.Request.class)
                .build();

		return ReactAgent.builder()
			.name("rag-agent")
			.description("A RAG (Retrieval Augmented Generation) agent that can answer questions "
					+ "about Spring AI Alibaba by retrieving relevant documentation from "
					+ "the knowledge base. The agent uses semantic search to find the most "
					+ "relevant information and synthesizes comprehensive answers.")
			.model(chatModel)
			.saver(new MemorySaver())
			.tools(searchCallback)
			.build();
	}

}
