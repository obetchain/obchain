/*
 * java-obc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-obc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obc.common;

import org.obc.common.parameter.CommonParameter;
import org.obc.core.ChainBaseManager;
import org.obc.core.config.args.Args;
import org.obc.core.db.Manager;

public interface Application {

  void setOptions(Args args);

  void init(CommonParameter parameter);

  void initServices(CommonParameter parameter);

  void startup();

  void shutdown();

  void startServices();

  void shutdownServices();

  void addService(Service service);

  Manager getDbManager();

  ChainBaseManager getChainBaseManager();

}
