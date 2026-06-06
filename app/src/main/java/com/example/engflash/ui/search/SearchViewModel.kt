package com.example.engflash.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engflash.EngFlashApplication
import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.model.Vocabulary
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val vocabularies: List<Vocabulary> = emptyList(),
    val grammarRules: List<GrammarRule> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EngFlashApplication
    private val searchVocabUseCase = app.searchVocabularyUseCase
    private val searchGrammarUseCase = app.searchGrammarUseCase

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            vocabularies = emptyList(),
                            grammarRules = emptyList(),
                            isSearching = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isSearching = true)
                        combine(
                            searchVocabUseCase(query),
                            searchGrammarUseCase(query)
                        ) { vocabs, grammars ->
                            _uiState.value = _uiState.value.copy(
                                vocabularies = vocabs,
                                grammarRules = grammars,
                                isSearching = false
                            )
                        }.collect()
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        queryFlow.value = query
    }
}
