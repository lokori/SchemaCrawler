/*
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2014, Sualeh Fatehi.
 * This library is free software; you can redistribute it and/or modify it under
 * the terms
 * of the GNU Lesser General Public License as published by the Free Software
 * Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package schemacrawler.tools.executable;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schemacrawler.SchemaCrawlerException;

/**
 * Command registry for mapping commands to executable.
 * 
 * @author Sualeh Fatehi
 */
public final class CommandRegistry
{

  private static final Logger LOGGER = Logger.getLogger(CommandRegistry.class
    .getName());

  private static Map<String, CommandProvider> loadCommandRegistry()
    throws SchemaCrawlerException
  {

    final List<CommandProvider> commandProviders = new ArrayList<CommandProvider>(Arrays
      .asList(new ExecutableCommandProvider("list",
                                            "schemacrawler.tools.text.schema.SchemaTextExecutable"),
              new ExecutableCommandProvider("schema",
                                            "schemacrawler.tools.text.schema.SchemaTextExecutable"),
              new ExecutableCommandProvider("details",
                                            "schemacrawler.tools.text.schema.SchemaTextExecutable"),
              new ExecutableCommandProvider("count",
                                            "schemacrawler.tools.text.operation.OperationExecutable"),
              new ExecutableCommandProvider("dump",
                                            "schemacrawler.tools.text.operation.OperationExecutable"),
              new ExecutableCommandProvider("script",
                                            "schemacrawler.tools.integration.scripting.ScriptExecutable"),
              new ExecutableCommandProvider("graph",
                                            "schemacrawler.tools.integration.graph.GraphExecutable")));

    try
    {
      final ServiceLoader<CommandProvider> serviceLoader = ServiceLoader
        .load(CommandProvider.class);
      for (final CommandProvider commandRegistryEntry: serviceLoader)
      {
        final String executableCommand = commandRegistryEntry.getCommand();
        LOGGER.log(Level.FINER, "Loading executable, " + executableCommand
                                + "="
                                + commandRegistryEntry.getClass().getName());
        commandProviders.add(commandRegistryEntry);
      }
    }
    catch (final Exception e)
    {
      throw new SchemaCrawlerException("Could not load extended command registry",
                                       e);
    }

    final Map<String, CommandProvider> commandRegistry = new HashMap<>();
    for (final CommandProvider commandProvider: commandProviders)
    {
      commandRegistry.put(commandProvider.getCommand(), commandProvider);
    }
    return commandRegistry;
  }

  private final Map<String, CommandProvider> commandRegistry;

  public CommandRegistry()
    throws SchemaCrawlerException
  {
    commandRegistry = loadCommandRegistry();
  }

  public String getHelpResource(final String command)
  {
    final String helpResource;
    if (commandRegistry.containsKey(command))
    {
      helpResource = commandRegistry.get(command).getHelpResource();
    }
    else
    {
      helpResource = null;
    }

    return helpResource;
  }

  public boolean hasCommand(final String command)
  {
    return commandRegistry.containsKey(command);
  }

  public Collection<String> lookupAvailableCommands()
  {
    final List<String> availableCommands = new ArrayList<>(commandRegistry.keySet());
    Collections.sort(availableCommands);
    return availableCommands;
  }

  Executable newExecutable(final String command)
    throws SchemaCrawlerException
  {
    final CommandProvider commandProvider;
    if (commandRegistry.containsKey(command))
    {
      commandProvider = commandRegistry.get(command);
    }
    else
    {
      commandProvider = new ExecutableCommandProvider(command,
                                                      "schemacrawler.tools.text.operation.OperationExecutable");
    }

    return commandProvider.newExecutable();
  }
}
