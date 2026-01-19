/*
 * the MIT License (MIT)
 *
 * Copyright (c) 2025-2026 Whirvex Software, LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * the above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.whirvex.karwei

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

private class TestContextElement :
    AbstractCoroutineContextElement(key = TestContextElement) {

    companion object Key : CoroutineContext.Key<TestContextElement>

}

private val TEST_TASK = task(name = "test")

internal class JobTest {

    @Test
    fun jobTaskTypesExtendJobTask() {
        assertIs<JobTask>(JobTask.RunBlocking)
        assertIs<JobTask>(JobTask.Launch)
        assertIs<JobTask>(JobTask.Async)
    }

    @Test
    fun runBlockingTaskUsesRunBlockingTaskByDefault() {
        val usedTask = runBlockingTask { taskContext.task }
        assertSame(JobTask.RunBlocking, usedTask)
    }

    @Test
    fun runBlockingTaskUsesGivenTask() {
        val usedTask = runBlockingTask(TEST_TASK) { taskContext.task }
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun runBlockingTaskWithNameUsesGivenName() {
        val taskName = runBlockingTask(TEST_TASK.name) { taskContext.task.name }
        assertEquals(TEST_TASK.name, taskName)
    }

    @Test
    fun taskRunBlockingUsesTaskInstance() {
        val usedTask = TEST_TASK.runBlocking { taskContext.task }
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun taskRunnableRunBlockingUsesTaskInstance() {
        val usedTask = TEST_TASK { taskContext.task }.runBlocking()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun scopedRunBlockingTaskUsesRunBlockingTaskByDefault(): Unit = runBlocking {
        val usedTask = runBlockingTask { taskContext.task }
        assertSame(JobTask.RunBlocking, usedTask)
    }

    @Test
    fun scopedRunBlockingTaskUsesGivenTask(): Unit = runBlocking {
        val usedTask = runBlockingTask(TEST_TASK) { taskContext.task }
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun scopedRunBlockingTaskOnlyInheritsTaskContextElements(): Unit = runBlocking {
        runBlockingTask(context = TestContextElement()) {
            val coroutineContext = currentCoroutineContext()
            assertNotNull(coroutineContext[TestContextElement])

            runBlockingTask {
                val coroutineContext = currentCoroutineContext()
                assertNotNull(coroutineContext[LiveTaskContextElement])
                assertNull(coroutineContext[TestContextElement])
            }
        }
    }

    @Test
    fun scopedRunBlockingTaskWithNameUsesGivenName(): Unit = runBlocking {
        val taskName = runBlockingTask(TEST_TASK.name) { taskContext.task.name }
        assertEquals(TEST_TASK.name, taskName)
    }

    @Test
    fun scopedTaskRunBlockingUsesTaskInstance(): Unit = runBlocking {
        val usedTask = TEST_TASK.runBlocking { taskContext.task }
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun scopedTaskRunnableRunBlockingUsesTaskInstance(): Unit = runBlocking {
        val usedTask = TEST_TASK { taskContext.task }.runBlocking()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun launchTaskUsesLaunchTaskByDefault(): Unit = runBlocking {
        lateinit var usedTask: Task
        launchTask { usedTask = taskContext.task }.join()
        assertSame(JobTask.Launch, usedTask)
    }

    @Test
    fun launchTaskUsesGivenTask(): Unit = runBlocking {
        lateinit var usedTask: Task
        launchTask(TEST_TASK) { usedTask = taskContext.task }.join()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun launchTaskInheritsAllContextElements(): Unit = runBlocking {
        launchTask(context = TestContextElement()) {
            val coroutineContext = currentCoroutineContext()
            assertNotNull(coroutineContext[TestContextElement])

            launchTask {
                val coroutineContext = currentCoroutineContext()
                assertNotNull(coroutineContext[LiveTaskContextElement])
                assertNotNull(coroutineContext[TestContextElement])
            }
        }
    }

    @Test
    fun launchTaskWithNameUsesGivenName(): Unit = runBlocking {
        lateinit var taskName: String
        launchTask(TEST_TASK.name) { taskName = taskContext.task.name }.join()
        assertEquals(TEST_TASK.name, taskName)
    }

    @Test
    fun taskLaunchUsesTaskInstance(): Unit = runBlocking {
        lateinit var usedTask: Task
        TEST_TASK.launch { usedTask = taskContext.task }.join()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun taskRunnableLaunchUsesTaskInstance(): Unit = runBlocking {
        lateinit var usedTask: Task
        TEST_TASK { usedTask = taskContext.task }.launch().join()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun asyncTaskUsesLaunchTaskByDefault(): Unit = runBlocking {
        val usedTask = asyncTask { taskContext.task }.await()
        assertSame(JobTask.Async, usedTask)
    }

    @Test
    fun asyncTaskUsesGivenTask(): Unit = runBlocking {
        val usedTask = asyncTask(TEST_TASK) { taskContext.task }.await()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun asyncTaskInheritsAllContextElements(): Unit = runBlocking {
        asyncTask(context = TestContextElement()) {
            val coroutineContext = currentCoroutineContext()
            assertNotNull(coroutineContext[TestContextElement])

            asyncTask {
                val coroutineContext = currentCoroutineContext()
                assertNotNull(coroutineContext[LiveTaskContextElement])
                assertNotNull(coroutineContext[TestContextElement])
            }
        }
    }

    @Test
    fun asyncTaskWithNameUsesGivenName(): Unit = runBlocking {
        val taskName = asyncTask(TEST_TASK.name) { taskContext.task.name }.await()
        assertEquals(TEST_TASK.name, taskName)
    }

    @Test
    fun taskAsyncUsesTaskInstance(): Unit = runBlocking {
        val usedTask = TEST_TASK.async { taskContext.task }.await()
        assertSame(TEST_TASK, usedTask)
    }

    @Test
    fun taskRunnableAsyncUsesTaskInstance(): Unit = runBlocking {
        val usedTask = TEST_TASK { taskContext.task }.async().await()
        assertSame(TEST_TASK, usedTask)
    }

}
