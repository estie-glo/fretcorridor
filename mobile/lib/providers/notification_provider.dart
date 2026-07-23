import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/notification_model.dart';
import 'dio_provider.dart';

class NotificationState {
  final List<NotificationModel> notifications;
  final int nonLues;
  final bool chargement;
  final String? erreur;

  const NotificationState({
    this.notifications = const [],
    this.nonLues = 0,
    this.chargement = false,
    this.erreur,
  });

  NotificationState copyWith({
    List<NotificationModel>? notifications,
    int? nonLues,
    bool? chargement,
    String? erreur,
  }) {
    return NotificationState(
      notifications: notifications ?? this.notifications,
      nonLues: nonLues ?? this.nonLues,
      chargement: chargement ?? this.chargement,
      erreur: erreur,
    );
  }
}

class NotificationNotifier extends StateNotifier<NotificationState> {
  final Dio _dio;

  NotificationNotifier(this._dio) : super(const NotificationState());

  Future<void> charger() async {
    state = state.copyWith(chargement: true, erreur: null);
    try {
      final listRes = await _dio.get('/notifications');
      final countRes = await _dio.get('/notifications/non-lues');
      final list = (listRes.data as List)
          .map((e) => NotificationModel.fromJson(e))
          .toList();
      final count = (countRes.data['count'] as num?)?.toInt() ?? 0;

      state = state.copyWith(
        chargement: false,
        notifications: list,
        nonLues: count,
      );
    } on DioException catch (e) {
      state = state.copyWith(
        chargement: false,
        erreur: 'Erreur : ${e.message}',
      );
    }
  }

  Future<void> marquerLue(String id) async {
    try {
      await _dio.patch('/notifications/$id/lue');
      state = state.copyWith(
        notifications: state.notifications
            .map((n) => n.id == id
                ? NotificationModel(
                    id: n.id,
                    canal: n.canal,
                    type: n.type,
                    titreFr: n.titreFr,
                    titreEn: n.titreEn,
                    corpsFr: n.corpsFr,
                    corpsEn: n.corpsEn,
                    lue: true,
                    dateCreation: n.dateCreation,
                  )
                : n)
            .toList(),
        nonLues: state.nonLues > 0 ? state.nonLues - 1 : 0,
      );
    } on DioException {
      // silencieux — rechargement au prochain accès
    }
  }
}

final notificationProvider =
    StateNotifierProvider<NotificationNotifier, NotificationState>((ref) {
  return NotificationNotifier(ref.watch(dioProvider));
});
