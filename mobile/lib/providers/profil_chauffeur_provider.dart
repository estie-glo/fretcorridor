import 'package:dio/dio.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:image_picker/image_picker.dart';
import '../models/chauffeur_model.dart';
import 'dio_provider.dart';

class ProfilChauffeurState {
  final ChauffeurModel? chauffeur;
  final bool chargement;
  final bool uploadEnCours;
  final String? erreur;
  final String? succes;

  const ProfilChauffeurState({
    this.chauffeur,
    this.chargement = false,
    this.uploadEnCours = false,
    this.erreur,
    this.succes,
  });

  ProfilChauffeurState copyWith({
    ChauffeurModel? chauffeur,
    bool? chargement,
    bool? uploadEnCours,
    String? erreur,
    String? succes,
  }) {
    return ProfilChauffeurState(
      chauffeur: chauffeur ?? this.chauffeur,
      chargement: chargement ?? this.chargement,
      uploadEnCours: uploadEnCours ?? this.uploadEnCours,
      erreur: erreur,
      succes: succes,
    );
  }
}

class ProfilChauffeurNotifier extends StateNotifier<ProfilChauffeurState> {
  final Dio _dio;
  final ImagePicker _picker = ImagePicker();

  ProfilChauffeurNotifier(this._dio) : super(const ProfilChauffeurState());

  Future<void> chargerProfil() async {
    state = state.copyWith(chargement: true, erreur: null);
    try {
      final response = await _dio.get('/chauffeurs/me');
      state = state.copyWith(
        chargement: false,
        chauffeur: ChauffeurModel.fromJson(response.data),
      );
    } on DioException catch (e) {
      state = state.copyWith(
        chargement: false,
        erreur: e.response?.statusCode == 404
            ? 'Profil chauffeur introuvable'
            : 'Erreur réseau : ${e.message}',
      );
    }
  }

  Future<void> uploaderDocument(String typeDocument) async {
    final chauffeur = state.chauffeur;
    if (chauffeur == null) return;

    final image = await _picker.pickImage(
      source: ImageSource.camera,
      imageQuality: 80,
      maxWidth: 1600,
    );
    if (image == null) return;

    state = state.copyWith(uploadEnCours: true, erreur: null, succes: null);
    try {
      final formData = FormData.fromMap({
        'fichier': await MultipartFile.fromFile(
          image.path,
          filename: image.name,
        ),
        'typeDocument': typeDocument,
        'chauffeurId': chauffeur.id,
      });

      await _dio.post('/kyc/documents', data: formData);
      await chargerProfil();
      state = state.copyWith(
        uploadEnCours: false,
        succes: 'Document $typeDocument envoyé ✅',
      );
    } on DioException catch (e) {
      state = state.copyWith(
        uploadEnCours: false,
        erreur: e.response?.statusCode == 403
            ? 'Accès refusé pour cet upload'
            : 'Échec upload : ${e.message}',
      );
    }
  }
}

final profilChauffeurProvider =
    StateNotifierProvider<ProfilChauffeurNotifier, ProfilChauffeurState>((ref) {
  return ProfilChauffeurNotifier(ref.watch(dioProvider));
});
