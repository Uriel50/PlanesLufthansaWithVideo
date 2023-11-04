package com.camu.planeslufthansa.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.camu.planeslufthansa.R
import com.camu.planeslufthansa.application.PlanesRFApp
import com.camu.planeslufthansa.data.PlaneRepository
import com.camu.planeslufthansa.data.remote.model.PlaneDetailDto
import com.camu.planeslufthansa.databinding.FragmentPlaneDetailBinding
import com.camu.planeslufthansa.util.Constants
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private const val PLANE_ID = "plane_id"

class PlaneDetailFragment : Fragment(){

    private var planeId: String? = null

    private var _binding: FragmentPlaneDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: PlaneRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            planeId = args.getString(PLANE_ID)

            val idRecibido = getString(R.string.id_recibido, planeId)
            Log.d(Constants.LOGTAG, idRecibido)

            repository = PlanesRFApp().repository

            lifecycleScope.launch{

                planeId?.let { id ->
                    val call: Call<PlaneDetailDto> = repository.getPlaneDetailApiary(id)

                    call.enqueue(object: Callback<PlaneDetailDto>{
                        override fun onResponse(
                            call: Call<PlaneDetailDto>,
                            response: Response<PlaneDetailDto>
                        ) {

                            binding.apply {
                                pbLoading.visibility = View.GONE

                                val context = binding.root.context

                                tvTitle.text =  response.body()?.title

                                tvLength.text = context.getString(R.string.longitud, response.body()?.length)
                                tvHeigth.text = context.getString(R.string.altura,response.body()?.height)
                                tvEngine.text = context.getString(R.string.motor,response.body()?.engine)
                                tvWinspan.text = context.getString(R.string.envergadura,response.body()?.wingspan)
                                tvMaximumSpeed.text = context.getString(R.string.velocidadmax,response.body()?.maximumSpeed)
                                tvAutonomy.text = context.getString(R.string.autonomia,response.body()?.autonomy)





                                binding.tvVideo.apply {

                                    val mediaController = MediaController(requireContext())
                                    var videoReady=false



                                    setMediaController(mediaController)
                                    setVideoPath(response.body()?.video)

                                    setOnPreparedListener { mediaPlayer ->
                                        // Ocultar el ProgressBar una vez que el video esté listo
                                        binding.pbLoadingVideo.visibility = View.GONE
                                        mediaPlayer.start()
                                        mediaController.show(0)
                                        videoReady=true
                                    }

                                    if (!videoReady){
                                        mediaController.setAnchorView(binding.tvVideo)
                                    }
                                    else{
                                        binding.pbLoadingVideo.visibility = View.VISIBLE
                                    }


                                    setOnCompletionListener { mediaPlayer ->
                                        // Reproducir el video nuevamente desde el inicio cuando termine
                                        mediaPlayer.seekTo(0)
                                        mediaPlayer.start()
                                    }

                                }



                                Glide.with(requireContext())
                                    .load(response.body()?.image)
                                    .into(ivImage)
                            }
                        }

                        override fun onFailure(call: Call<PlaneDetailDto>, t: Throwable) {
                            binding.pbLoading.visibility = View.GONE

                            val mensajeNoConexion = getString(R.string.no_conexion)
                            Toast.makeText(requireActivity(), mensajeNoConexion, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaneDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        @JvmStatic
        fun newInstance(planeId: String) =
            PlaneDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(PLANE_ID, planeId)
                }
            }

    }
}