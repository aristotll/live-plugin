package liveplugin.implementation

import com.intellij.openapi.actionSystem.DataContext
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.NotNull

class MapDataContext implements DataContext {
	private final Map map

	MapDataContext(Map map = [:]) {
		this.map = map
	}

	@Override Object getData(@NonNls String dataId) {
		map.get(dataId)
	}

	MapDataContext put(@NotNull String key, Object value) {
		map.put(key, value)
		this
	}
}
