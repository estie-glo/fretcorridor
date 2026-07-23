import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/mission_model.dart';
import 'dio_provider.dart';

class MatchsState {
  final List<MissionModel> matchs;
  final bool chargement;
  final String? erreur;

  const MatchsState({
    this.matchs = const [],
    this.chargement = false,
    this.erreur,
  });

  MatchsState copyWith({
    List<MissionModel>? matchs,
    bool? chargement,
    String? erreur,
  }) {
    return MatchsState(
      matchs: matchs ?? this.matchs,
      chargement: chargement ?? this.chargement,
      erreur: erreur,
    );
  }
}

class MatchsNotifier extends StateNotifier<MatchsState> {
  final Dio _dio;

  MatchsNotifier(this._dio) : super(const MatchsState());

  Future<void> charger() async {
    state = state.copyWith(chargement: true, erreur: null);
    try {
      final response = await _dio.get('/missions/matchs');
      final matchs = (response.data as List)
          .map((e) => MissionModel.fromJson(e))
          .toList();
      state = state.copyWith(chargement: false, matchs: matchs);
    } on DioException catch (e) {
      state = state.copyWith(
        chargement: false,
        erreur: 'Erreur : ${e.message}',
      );
    }
  }
}

final matchsProvider =
    StateNotifierProvider<MatchsNotifier, MatchsState>((ref) {
  return MatchsNotifier(ref.watch(dioProvider));
});
