import 'package:flutter_riverpod/legacy.dart';
import 'package:dio/dio.dart';
import '../models/axe_model.dart';
import 'dio_provider.dart';

class AxeState {
  final List<AxeModel> axes;
  final bool chargement;
  final String? erreur;

  const AxeState({this.axes = const [], this.chargement = false, this.erreur});

  AxeState copyWith({List<AxeModel>? axes, bool? chargement, String? erreur}) {
    return AxeState(
      axes: axes ?? this.axes,
      chargement: chargement ?? this.chargement,
      erreur: erreur,
    );
  }
}

class AxeNotifier extends StateNotifier<AxeState> {
  final Dio _dio;
  AxeNotifier(this._dio) : super(const AxeState()) {
    chargerAxes();
  }

  Future<void> chargerAxes() async {
    state = state.copyWith(chargement: true, erreur: null);
    try {
      final response = await _dio.get('/axes');
      state = state.copyWith(
        chargement: false,
        axes: (response.data as List).map((e) => AxeModel.fromJson(e)).toList(),
      );
    } on DioException catch (e) {
      state = state.copyWith(
        chargement: false,
        erreur: 'Erreur réseau : ${e.message}',
      );
    }
  }
}

final axeProvider = StateNotifierProvider<AxeNotifier, AxeState>((ref) {
  final dio = ref.watch(dioProvider);
  return AxeNotifier(dio);
});
