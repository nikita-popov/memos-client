package xyz.polyserv.memos.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import androidx.compose.material3.MaterialTheme

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    Markdown(
        content = markdown,
        colors = markdownColor(
            text = MaterialTheme.colorScheme.onBackground,
            codeText = MaterialTheme.colorScheme.onSecondaryContainer,
            codeBackground = MaterialTheme.colorScheme.secondaryContainer,
            linkText = MaterialTheme.colorScheme.primary,
        ),
        typography = markdownTypography(
            h1 = MaterialTheme.typography.headlineLarge,
            h2 = MaterialTheme.typography.headlineMedium,
            h3 = MaterialTheme.typography.headlineSmall,
            h4 = MaterialTheme.typography.titleLarge,
            h5 = MaterialTheme.typography.titleMedium,
            h6 = MaterialTheme.typography.titleSmall,
            text = MaterialTheme.typography.bodyLarge,
            code = MaterialTheme.typography.bodyMedium,
            quote = MaterialTheme.typography.bodyMedium,
            paragraph = MaterialTheme.typography.bodyLarge,
            ordered = MaterialTheme.typography.bodyLarge,
            bullet = MaterialTheme.typography.bodyLarge,
            list = MaterialTheme.typography.bodyLarge,
        ),
        modifier = modifier
    )
}
