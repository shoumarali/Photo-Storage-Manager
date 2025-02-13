package com.alishoumar.androidstorage.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ItemPhotoBinding

/*
list adapter is just extension of RecyclerView.Adapter it computes diffs between list on background
thread using Async list diff.You can obviously create an recyclerview adapter to work in the same way
its just list adapter already work on a principle out of the box. It defines a contract to force
DiffUtil uses hence both of its constructor needs a diff checker. Performance will be the same if you
used listAdapter or recyclerviewAdapter with asyncDiffChecker. Without AsyncDiffChecker listAdapter's
performance will be better
 */


class InternalStoragePhotoAdapter(
    private val onPhotoClick : (internalPhoto : InternalStoragePhoto) -> Unit
) : ListAdapter<InternalStoragePhoto, InternalStoragePhotoAdapter.InternalStoragePhotoViewHolder>(
    Companion
) {


    inner class InternalStoragePhotoViewHolder(
        val binding: ItemPhotoBinding
    ): RecyclerView.ViewHolder(binding.root)

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
            return oldItem.name == newItem.name && oldItem.bmp.sameAs(newItem.bmp)
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

            ivPhoto.setImageBitmap(photo.bmp)

            val aspectRatio = photo.bmp.width.toFloat() / photo.bmp.height.toFloat()
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id, aspectRatio.toString())
                applyTo(root)
            }

            ivPhoto.setOnLongClickListener{
                onPhotoClick(photo)
                true
            }
        }
    }
}