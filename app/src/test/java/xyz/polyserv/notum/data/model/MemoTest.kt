package xyz.polyserv.notum.data.model

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class MemoTest {

    @Test
    fun `memo creation with default values`() {
        val memo = Memo(
            content = "Test content"
        )

        assertNotNull(memo.id)
        assertEquals("Test content", memo.content)
        assertEquals(SyncStatus.PENDING, memo.syncStatus)
        assertTrue(memo.isLocalOnly)
        assertEquals(MemoState.NORMAL, memo.state)
        assertEquals(Visibility.PRIVATE, memo.visibility)
        assertFalse(memo.pinned)
    }

    @Test
    fun `memo with custom id`() {
        val customId = UUID.randomUUID().toString()
        val memo = Memo(
            id = customId,
            content = "Test content"
        )

        assertEquals(customId, memo.id)
    }

    @Test
    fun `memo sync status changes`() {
        var memo = Memo(content = "Test", syncStatus = SyncStatus.PENDING)
        assertEquals(SyncStatus.PENDING, memo.syncStatus)

        memo = memo.copy(syncStatus = SyncStatus.SYNCING)
        assertEquals(SyncStatus.SYNCING, memo.syncStatus)

        memo = memo.copy(syncStatus = SyncStatus.SYNCED, isLocalOnly = false)
        assertEquals(SyncStatus.SYNCED, memo.syncStatus)
        assertFalse(memo.isLocalOnly)
    }

    @Test
    fun `memo visibility levels`() {
        val privateMemo = Memo(content = "Private", visibility = Visibility.PRIVATE)
        val protectedMemo = Memo(content = "Protected", visibility = Visibility.PROTECTED)
        val publicMemo = Memo(content = "Public", visibility = Visibility.PUBLIC)

        assertEquals(Visibility.PRIVATE, privateMemo.visibility)
        assertEquals(Visibility.PROTECTED, protectedMemo.visibility)
        assertEquals(Visibility.PUBLIC, publicMemo.visibility)
    }

    @Test
    fun `memo state changes`() {
        var memo = Memo(content = "Test", state = MemoState.NORMAL)
        assertEquals(MemoState.NORMAL, memo.state)

        memo = memo.copy(state = MemoState.ARCHIVED)
        assertEquals(MemoState.ARCHIVED, memo.state)
    }

    @Test
    fun `memo pinning`() {
        var memo = Memo(content = "Test", pinned = false)
        assertFalse(memo.pinned)

        memo = memo.copy(pinned = true)
        assertTrue(memo.pinned)
    }

    @Test
    fun `memo toString contains essential info`() {
        val memo = Memo(
            id = "test-id",
            serverId = "server-123",
            content = "This is a long content that should be truncated in toString",
            syncStatus = SyncStatus.SYNCED,
            isLocalOnly = false
        )

        val stringRepresentation = memo.toString()
        assertTrue(stringRepresentation.contains("test-id"))
        assertTrue(stringRepresentation.contains("server-123"))
        assertTrue(stringRepresentation.contains("SYNCED"))
        assertTrue(stringRepresentation.contains("isLocalOnly=false"))
    }

    @Test
    fun `memo with server data`() {
        val memo = Memo(
            content = "Synced content",
            name = "memos/123",
            uid = "abc-def-ghi",
            creator = "users/john",
            serverId = "123",
            isLocalOnly = false,
            syncStatus = SyncStatus.SYNCED
        )

        assertEquals("memos/123", memo.name)
        assertEquals("abc-def-ghi", memo.uid)
        assertEquals("users/john", memo.creator)
        assertEquals("123", memo.serverId)
        assertFalse(memo.isLocalOnly)
    }

    @Test
    fun `memo timestamps are stored`() {
        val createTime = "2024-01-01T10:00:00Z"
        val updateTime = "2024-01-01T11:00:00Z"
        val displayTime = "2024-01-01T12:00:00Z"

        val memo = Memo(
            content = "Test",
            createTime = createTime,
            updateTime = updateTime,
            displayTime = displayTime
        )

        assertEquals(createTime, memo.createTime)
        assertEquals(updateTime, memo.updateTime)
        assertEquals(displayTime, memo.displayTime)
    }

    @Test
    fun `memo copy creates new instance`() {
        val original = Memo(content = "Original")
        val copied = original.copy(content = "Modified")

        assertNotEquals(original.content, copied.content)
        assertEquals(original.id, copied.id)
    }
}
