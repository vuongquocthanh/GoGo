package ds.vuongquocthanh.gogo.mvp

interface Presenter{
    fun attachView(view : View)
    fun dispose()
}