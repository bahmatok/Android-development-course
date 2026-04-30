package com.example.calculator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.view.ViewGroup as AndroidViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentBasicPadBinding

class BasicPadFragment : Fragment() {
    private var _binding: FragmentBasicPadBinding? = null
    private val binding get() = _binding!!
    private var listener: InputActionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? InputActionListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicPadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindClickListenersRecursively(binding.root)
    }

    private fun bindClickListenersRecursively(view: View) {
        when (view) {
            is Button -> view.setOnClickListener { listener?.onInputAction(view.tag.toString()) }
            is AndroidViewGroup -> {
                for (i in 0 until view.childCount) {
                    bindClickListenersRecursively(view.getChildAt(i))
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
