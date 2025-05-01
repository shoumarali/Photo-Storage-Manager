package com.alishoumar.androidstorage.presentation.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.CachePolicy
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.data.utils.CryptoManager
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ItemPhotoBinding
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.presentation.adapter.fetcher.EncryptedImageFetcher
import com.alishoumar.androidstorage.presentation.adapter.fetcher.EncryptedImageFetcherFactory

/*
list adapter is just extension of RecyclerView.Adapter it computes diffs between list on background
thread using Async list diff.You can obviously create an recyclerview adapter to work in the same way
its just list adapter already work on a principle out of the box. It defines a contract to force
DiffUtil uses hence both of its constructor needs a diff checker. Performance will be the same if you
used listAdapter or recyclerviewAdapter with asyncDiffChecker. Without AsyncDiffChecker listAdapter's
performance will be better
 */


class InternalStoragePhotoAdapter(
    private val cryptoManager: CryptoManager,
    private val onImageClick : (internalPhoto : InternalStoragePhoto) -> Unit
) : ListAdapter<InternalStoragePhoto, InternalStoragePhotoAdapter.InternalStoragePhotoViewHolder>(
    Companion
) {


    inner class InternalStoragePhotoViewHolder(
        val binding: ItemPhotoBinding
    ): RecyclerView.ViewHolder(binding.root){
        private var currentPhoto: InternalStoragePhoto? = null
        private var imageLoader : ImageLoader? = null

        init {
            imageLoader = ImageLoader.Builder(itemView.context)
                .components {
                    add(EncryptedImageFetcherFactory(
                        cryptoManager,
                        itemView.context))
                }
                .build()

            binding.ivPhoto.setOnClickListener {
                currentPhoto?.let { onImageClick(it) }
            }
        }

        fun bind(photo: InternalStoragePhoto) {
            currentPhoto = photo

            binding.ivPhoto.load(photo.filePath, imageLoader!!) {
                crossfade(true)
                size(344, 344)
                diskCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
                placeholder(R.drawable.baseline_photo_24)
            }

            val photoWidthAndHeight = photo.name.split("#")[1].split("x")

            val aspectRatio = photoWidthAndHeight[0].toFloat() / photoWidthAndHeight[1].removeSuffix(".enc").toFloat()
            ConstraintSet().apply {
                clone(binding.root)
                setDimensionRatio(binding.ivPhoto.id, aspectRatio.toString())
                applyTo(binding.root)
            }
        }

    }

    companion object : DiffUtil.ItemCallback<InternalStoragePhoto>(){
        override fun areItemsTheSame(
            oldItem: InternalStoragePhoto,
            newItem: InternalStoragePhoto
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: InternalStoragePhoto,
            newItem: InternalStoragePhoto
        ): Boolean {
            return oldItem.name == newItem.name
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InternalStoragePhotoViewHolder {
        return InternalStoragePhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: InternalStoragePhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            holder.bind(photo)
        }
    }
}