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
package com.cloud.alibaba.ai.example.agent.rag;

import com.cloud.alibaba.ai.example.agent.rag.entity.*;
import com.cloud.alibaba.ai.example.agent.rag.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PsqlTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationTagRepository organizationTagRepository;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private ChunkInfoRepository chunkInfoRepository;

    @Autowired
    private DocumentVectorRepository documentVectorRepository;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== User Tests ====================

    @Test
    void testCreateUser() {
        User user = new User("testuser", "password123");
        user.setRole(UserRole.USER);
        user.setOrgTags("org1,org2");
        user.setPrimaryOrg("org1");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals(UserRole.USER, savedUser.getRole());
        assertEquals("org1,org2", savedUser.getOrgTags());
        assertEquals("org1", savedUser.getPrimaryOrg());
    }

    @Test
    void testFindUserByUsername() {
        User user = new User("finduser", "password123");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("finduser");

        assertTrue(foundUser.isPresent());
        assertEquals("finduser", foundUser.get().getUsername());
    }

    @Test
    void testExistsByUsername() {
        User user = new User("existuser", "password123");
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("existuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testUserRoleDefault() {
        User user = new User("defaultrole", "password123");
        User savedUser = userRepository.save(user);

        assertEquals(UserRole.USER, savedUser.getRole());
    }

    @Test
    void testUserRoleAdmin() {
        User user = new User("adminuser", "password123");
        user.setRole(UserRole.ADMIN);
        User savedUser = userRepository.save(user);

        assertEquals(UserRole.ADMIN, savedUser.getRole());
    }

    // ==================== OrganizationTag Tests ====================

    @Test
    void testCreateOrganizationTag() {
        User user = new User("tagcreator", "password123");
        userRepository.save(user);

        OrganizationTag tag = new OrganizationTag("tag-001", "测试标签", user);
        tag.setDescription("这是一个测试标签");

        OrganizationTag savedTag = organizationTagRepository.save(tag);

        assertEquals("tag-001", savedTag.getTagId());
        assertEquals("测试标签", savedTag.getName());
        assertEquals("这是一个测试标签", savedTag.getDescription());
        assertEquals(user.getId(), savedTag.getCreatedBy().getId());
    }

    @Test
    void testFindOrganizationTagsByParentTag() {
        User user = new User("parenttaguser", "password123");
        userRepository.save(user);

        OrganizationTag parentTag = new OrganizationTag("parent-001", "父标签", user);
        organizationTagRepository.save(parentTag);

        OrganizationTag childTag1 = new OrganizationTag("child-001", "子标签1", user);
        childTag1.setParentTag("parent-001");
        organizationTagRepository.save(childTag1);

        OrganizationTag childTag2 = new OrganizationTag("child-002", "子标签2", user);
        childTag2.setParentTag("parent-001");
        organizationTagRepository.save(childTag2);

        List<OrganizationTag> childTags = organizationTagRepository.findByParentTag("parent-001");

        assertEquals(2, childTags.size());
    }

    // ==================== Vector Store Tests ====================

    @Test
    void testDeleteAllVectorDataAlternative() {
        // 另一种删除所有向量数据的方式：使用 fileMd5 不为空字符串的条件
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.ne("fileMd5", "").build();
        vectorStore.delete(expression);
        System.out.println("所有向量数据已删除（使用 ne 条件）");
    }

    @Test
    void testDeleteAllVectorDataByJdbc() {
        // 最直接的方式：使用 JDBC 直接删除 vector_store 表的所有数据
        int deletedCount = jdbcTemplate.update("DELETE FROM vector_store");
        System.out.println("已删除 vector_store 表中的 " + deletedCount + " 条记录");
    }

    @Test
    void testDeleteAllVectorDataByJdbcTruncate() {
        // 使用 TRUNCATE 快速清空 vector_store 表（更快，但无法回滚）
        jdbcTemplate.execute("TRUNCATE TABLE vector_store");
        System.out.println("vector_store 表已清空（使用 TRUNCATE）");
    }

    @Test
    void testShowVectorStoreCount() {
        // 查询 vector_store 表中的记录数
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);
        System.out.println("vector_store 表中共有 " + count + " 条记录");
    }
}