package com.example.engflash.util

data class GrammarTip(val title: String, val description: String)

object GrammarTipsPool {
    private val studyTips = listOf(
        GrammarTip(
            "Since vs For",
            "Tìm hiểu tại sao nhiều người học hay nhầm lẫn giữa 'Since' và 'For'."
        ),
        GrammarTip(
            "Past Simple vs Present Perfect",
            "Khi nào dùng 'I went' và khi nào dùng 'I have gone'? Cùng khám phá!"
        ),
        GrammarTip(
            "Must vs Can't",
            "'Must' dùng cho suy đoán khẳng định, 'can't' cho phủ định — KHÔNG dùng 'mustn't'."
        ),
        GrammarTip(
            "Dấu hiệu nhận biết thì",
            "Yesterday, last week → Past Simple. Already, just, ever → Present Perfect."
        ),
        GrammarTip(
            "Câu điều kiện loại 2",
            "Dùng 'were' cho mọi chủ ngữ: 'If I were you...' — KHÔNG viết 'If I was'."
        ),
        GrammarTip(
            "Gerund vs Infinitive",
            "Sau 'enjoy, avoid, suggest' → V-ing. Sau 'want, need, decide' → to + V."
        ),
        GrammarTip(
            "Câu bị động",
            "Cấu trúc: S + be + V3/V-ed. 'The book was written by him' — chú ý 'by'."
        ),
    )

    private val aiInsights = listOf(
        GrammarTip(
            "Phát âm phản xạ",
            "Kiểm tra phát âm các động từ bất quy tắc ở phân từ hai của bạn."
        ),
        GrammarTip(
            "Lỗi phổ biến nhất",
            "90% người Việt viết 'Since 3 years' thay vì 'For 3 years'. Bạn có mắc không?"
        ),
        GrammarTip(
            "Mẹo nhớ Modal Verbs",
            "Must = 90% chắc chắn, Might = 50%, Can't = 0% khả năng xảy ra."
        ),
        GrammarTip(
            "Bẫy thường gặp",
            "Đừng nói 'I am agree'. Đúng là 'I agree' — 'agree' là động từ, không phải tính từ."
        ),
        GrammarTip(
            "Present Perfect với 'just'",
            "'I have just eaten' diễn tả việc vừa xảy ra — 'just' luôn đứng sau have/has."
        ),
        GrammarTip(
            "Prepositions of time",
            "'In' cho tháng/năm, 'On' cho ngày cụ thể, 'At' cho giờ — không dùng lẫn lộn."
        ),
        GrammarTip(
            "Inversion sau 'Rarely'",
            "'Rarely do I see...' — Sau các từ phủ định đầu câu, đảo ngữ là bắt buộc."
        ),
    )

    fun getRandomStudyTip(): GrammarTip = studyTips.random()
    fun getRandomAiInsight(): GrammarTip = aiInsights.random()
}
