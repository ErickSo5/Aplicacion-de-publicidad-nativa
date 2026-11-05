
package com.example.aderick

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AdErick"

        private const val SAMPLE_APP_ID =
            "ca-app-pub-3940256099942544~3347511713"

        private const val NATIVE_AD_UNIT_ID =
            "ca-app-pub-3940256099942544/2247696110"
    }

    private var nativeAd: NativeAd? = null
    private var currentPage = 0

    private data class Recipe(
        val title: String,
        val emoji: String,
        val time: String,
        val ingredients: String,
        val preparation: String
    )

    private val recipes = listOf(
        Recipe(
            title = "Hotcakes caseros",
            emoji = "🥞",
            time = "20 minutos",
            ingredients = """
                • 1 taza de harina
                • 1 huevo
                • 3/4 de taza de leche
                • 2 cucharadas de azúcar
                • 1 cucharadita de polvo para hornear
                • 1 cucharada de mantequilla
            """.trimIndent(),
            preparation = """
                1. Mezcla la harina, el azúcar y el polvo para hornear.

                2. Agrega el huevo, la leche y la mantequilla derretida.

                3. Bate hasta obtener una mezcla uniforme.

                4. Calienta un sartén y agrega un poco de mantequilla.

                5. Vierte una porción de mezcla y cocina hasta que aparezcan burbujas.

                6. Voltea el hotcake y cocina el otro lado.

                7. Sirve con miel o fruta.
            """.trimIndent()
        ),
        Recipe(
            title = "Pasta cremosa",
            emoji = "🍝",
            time = "25 minutos",
            ingredients = """
                • 200 g de pasta
                • 1 taza de crema
                • 1 diente de ajo
                • 1 cucharada de mantequilla
                • 1/2 taza de queso rallado
                • Sal y pimienta al gusto
            """.trimIndent(),
            preparation = """
                1. Cocina la pasta en agua hirviendo con un poco de sal.

                2. Escurre la pasta cuando esté cocida.

                3. En otro sartén, derrite la mantequilla y sofríe el ajo picado.

                4. Agrega la crema y mezcla a fuego bajo.

                5. Incorpora el queso, la sal y la pimienta.

                6. Añade la pasta y mezcla hasta integrar.

                7. Sirve caliente.
            """.trimIndent()
        ),
        Recipe(
            title = "Limonada natural",
            emoji = "🍋",
            time = "10 minutos",
            ingredients = """
                • 4 limones
                • 1 litro de agua
                • 3 cucharadas de azúcar
                • Hielo al gusto
            """.trimIndent(),
            preparation = """
                1. Exprime los limones y retira las semillas.

                2. Vierte el jugo en una jarra.

                3. Agrega el agua y el azúcar.

                4. Mezcla hasta disolver el azúcar.

                5. Añade hielo y sirve fría.
            """.trimIndent()
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        setupRecipeNavigation()
        showRecipe()

        initializeGoogleMobileAds()
    }

    // Configurar botones de navegación
    private fun setupRecipeNavigation() {
        val previousButton = findViewById<Button>(R.id.btn_previous)
        val nextButton = findViewById<Button>(R.id.btn_next)

        previousButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                showRecipe()
            }
        }

        nextButton.setOnClickListener {
            if (currentPage < recipes.size - 1) {
                currentPage++
                showRecipe()
            }
        }
    }

    // Mostrar la receta correspondiente
    private fun showRecipe() {
        val recipe = recipes[currentPage]

        findViewById<TextView>(R.id.recipe_number).text =
            "RECETA ${currentPage + 1}"

        findViewById<TextView>(R.id.recipe_title).text =
            recipe.title

        findViewById<TextView>(R.id.recipe_emoji).text =
            recipe.emoji

        findViewById<TextView>(R.id.recipe_time).text =
            "⏱ ${recipe.time}"

        findViewById<TextView>(R.id.recipe_ingredients).text =
            recipe.ingredients

        findViewById<TextView>(R.id.recipe_preparation).text =
            recipe.preparation

        findViewById<TextView>(R.id.page_indicator).text =
            "${currentPage + 1} / ${recipes.size}"

        findViewById<Button>(R.id.btn_previous).isEnabled =
            currentPage > 0

        findViewById<Button>(R.id.btn_next).isEnabled =
            currentPage < recipes.size - 1
    }

    // Inicializar Google Mobile Ads
    private fun initializeGoogleMobileAds() {
        Thread {
            MobileAds.initialize(
                this@MainActivity,
                InitializationConfig.Builder(SAMPLE_APP_ID).build()
            ) {
                runOnUiThread {
                    Log.d(TAG, "SDK inicializado correctamente")
                    loadNativeAd()
                }
            }
        }.start()
    }

    // Solicitar un anuncio nativo
    private fun loadNativeAd() {
        Log.d(TAG, "Solicitando anuncio nativo...")

        val adRequest = NativeAdRequest.Builder(
            NATIVE_AD_UNIT_ID,
            listOf(NativeAd.NativeAdType.NATIVE)
        ).build()

        val adCallback = object : NativeAdLoaderCallback {

            override fun onNativeAdLoaded(loadedAd: NativeAd) {
                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        loadedAd.destroy()
                        return@runOnUiThread
                    }

                    Log.d(TAG, "Anuncio nativo cargado correctamente")

                    nativeAd?.destroy()
                    nativeAd = loadedAd

                    displayNativeAd(loadedAd)
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Error al cargar anuncio: ${adError.message}")
                Log.e(TAG, "Código de error: ${adError.code}")
            }
        }

        NativeAdLoader.load(adRequest, adCallback)
    }

    // Mostrar el anuncio nativo
    private fun displayNativeAd(ad: NativeAd) {
        val container = findViewById<FrameLayout>(
            R.id.native_ad_container
        )

        val adView = layoutInflater.inflate(
            R.layout.native_ad_layout,
            container,
            false
        ) as NativeAdView

        val attribution = adView.findViewById<TextView>(
            R.id.ad_attribution
        )

        val headline = adView.findViewById<TextView>(
            R.id.ad_headline
        )

        val advertiser = adView.findViewById<TextView>(
            R.id.ad_advertiser
        )

        val body = adView.findViewById<TextView>(
            R.id.ad_body
        )

        val icon = adView.findViewById<ImageView>(
            R.id.ad_app_icon
        )

        val mediaView = adView.findViewById<MediaView>(
            R.id.ad_media
        )

        val callToAction = adView.findViewById<Button>(
            R.id.ad_call_to_action
        )

        adView.advertiserView = advertiser
        adView.bodyView = body
        adView.callToActionView = callToAction
        adView.headlineView = headline
        adView.iconView = icon

        attribution.visibility = View.VISIBLE

        headline.text = ad.headline
        headline.visibility = assetVisibility(ad.headline)

        advertiser.text = ad.advertiser
        advertiser.visibility = assetVisibility(ad.advertiser)

        body.text = ad.body
        body.visibility = assetVisibility(ad.body)

        icon.setImageDrawable(ad.icon?.drawable)
        icon.visibility = assetVisibility(ad.icon)

        callToAction.text = ad.callToAction
        callToAction.visibility = assetVisibility(ad.callToAction)

        adView.registerNativeAd(ad, mediaView)

        container.removeAllViews()
        container.addView(adView)

        Log.d(TAG, "Anuncio agregado a la interfaz")
    }

    private fun assetVisibility(asset: Any?): Int {
        return if (asset == null) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }

    override fun onDestroy() {
        nativeAd?.destroy()
        nativeAd = null
        super.onDestroy()
    }
}
