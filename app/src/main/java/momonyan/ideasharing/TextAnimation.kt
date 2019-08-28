package momonyan.ideasharing

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.TextView
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


fun startTextAnimation(animationObj: TextView) {

    val mHandler = Handler()

    val mScheduledExecutor = Executors.newScheduledThreadPool(2)

    mScheduledExecutor.scheduleWithFixedDelay(object : Runnable {
        override fun run() {
            mHandler.post {
                animationObj.visibility = View.VISIBLE

                animateAlpha()
            }
        }


        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private fun animateAlpha() {

            // 実行するAnimatorのリスト
            val animatorList = ArrayList<Animator>()

            // alpha値を0から1へ1000ミリ秒かけて変化させる。
            val animeFadeIn = ObjectAnimator.ofFloat(animationObj, "alpha", 0f, 1f)
            animeFadeIn.duration = 1000

            // alpha値を1から0へ600ミリ秒かけて変化させる。
            val animeFadeOut = ObjectAnimator.ofFloat(animationObj, "alpha", 1f, 0f)
            animeFadeOut.duration = 600

            // 実行対象Animatorリストに追加。
            animatorList.add(animeFadeIn)
            animatorList.add(animeFadeOut)

            val animatorSet = AnimatorSet()

            // リストの順番に実行
            animatorSet.playSequentially(animatorList)

            animatorSet.start()
        }
    }, 0, 1700, TimeUnit.MILLISECONDS)

}
