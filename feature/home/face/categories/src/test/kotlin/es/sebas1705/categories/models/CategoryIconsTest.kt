package es.sebas1705.categories.models

import es.sebas1705.models.Categories
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconsTest {

    @Test
    fun `every category has its own icon`() {
        val shared = Categories.entries
            .groupBy { it.toCategoryIcon().name }
            .filterValues { it.size > 1 }

        assertTrue("Categories sharing an icon: $shared", shared.isEmpty())
    }
}
