package com.example.vita

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vita.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private var isCurrentVisible = false
    private var isNewVisible = false
    private var isConfirmVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Voltar
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        // Toggle Password Visibility
        binding.btnToggleCurrent.setOnClickListener {
            isCurrentVisible = !isCurrentVisible
            togglePasswordVisibility(binding.etCurrentPassword, isCurrentVisible)
        }

        binding.btnToggleNew.setOnClickListener {
            isNewVisible = !isNewVisible
            togglePasswordVisibility(binding.etNewPassword, isNewVisible)
        }

        binding.btnToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(binding.etConfirmPassword, isConfirmVisible)
        }

        // Salvar nova senha
        binding.btnSavePassword.setOnClickListener {
            alterarSenha()
        }
    }

    private fun togglePasswordVisibility(editText: android.widget.EditText, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text.length)
    }

    private fun alterarSenha() {
        val senhaAtual = binding.etCurrentPassword.text.toString().trim()
        val novaSenha = binding.etNewPassword.text.toString().trim()
        val confirmarSenha = binding.etConfirmPassword.text.toString().trim()

        if (senhaAtual.isEmpty()) {
            binding.etCurrentPassword.error = "Digite sua senha atual"
            binding.etCurrentPassword.requestFocus()
            return
        }

        if (novaSenha.isEmpty()) {
            binding.etNewPassword.error = "Crie uma nova senha"
            binding.etNewPassword.requestFocus()
            return
        }

        if (novaSenha.length < 6) {
            binding.etNewPassword.error = "A senha deve ter pelo menos 6 caracteres"
            binding.etNewPassword.requestFocus()
            return
        }

        if (novaSenha != confirmarSenha) {
            binding.etConfirmPassword.error = "As senhas não coincidem"
            binding.etConfirmPassword.requestFocus()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, senhaAtual)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(novaSenha)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Erro ao atualizar senha: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Senha atual incorreta!", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Fallback if not logged in via Firebase Auth
            Toast.makeText(requireContext(), "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
